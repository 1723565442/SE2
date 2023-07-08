package org.fffd.l23o6.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.util.strategy.payment.AliPayStrategy;
import org.fffd.l23o6.util.strategy.payment.PaymentStrategy;
import org.fffd.l23o6.util.strategy.payment.WechatStrategy;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

import static org.fffd.l23o6.util.strategy.payment.PaymentStrategy.strategy.ALIPAY;
import static org.fffd.l23o6.util.strategy.payment.PaymentStrategy.strategy.WECHAT;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
            Long seatNumber) {
        Long userId = userDao.findByUsername(username).getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = null;
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                seat = GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
                break;
            case NORMAL_SPEED:
                seat = KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
                break;
        }
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }
        //TODO: 添加 price 和 usePoints 和 mileagePoints 和 paymentStrategy
        TrainEntity trainEntity = trainDao.findById(trainId).get();
        RouteEntity routeEntity = routeDao.findById(trainEntity.getRouteId()).get();
        List<Long> stations = routeEntity.getStationIds();
        int startStationIdx = stations.indexOf(fromStationId), endStationIdx = stations.indexOf(toStationId);
        //设置变量
        PaymentStrategy.strategy paymentStrategy = ALIPAY;
        boolean usePoints = true;
        double mileagePoints = (endStationIdx - startStationIdx) * 100;
        double price = trainEntity.getTrainType() == TrainType.HIGH_SPEED
                ?GSeriesSeatStrategy.INSTANCE.getPrice(startStationIndex, endStationIndex, GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType))
                :KSeriesSeatStrategy.INSTANCE.getPrice(startStationIndex, endStationIndex, KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType));
        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat)
                .price(price).usePoints(usePoints).bonusPoints(mileagePoints).consumptionPoints(0).paymentStrategy(paymentStrategy)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId)
                .build();
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);
        return order.getId();
    }

    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        //TODO
        return orders.stream().map(order -> {
            UserEntity user = userDao.findById(order.getUserId()).get();
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            double balance;
            String paymentStrategy;
            switch (order.getPaymentStrategy()){
                case ALIPAY:
                    paymentStrategy = "支付宝";
                    balance = user.getAliBalance();
                    break;
                default:
                    paymentStrategy = "微信";
                    balance = user.getWechatBalance();
            }
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .price(order.getPrice()).bonusPoints(order.getBonusPoints()).usePoints(true).paymentStrategy(paymentStrategy)
                    .balance(balance).consumptionPoints(order.getConsumptionPoints()).leftPoints(user.getMileagePoints())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(train.getArrivalTimes().get(endIndex))
                    .build();
        }).collect(Collectors.toList());
    }

    public OrderVO getOrder(Long id) {
        //TODO
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        double balance;
        String paymentStrategy;
        switch (order.getPaymentStrategy()){
            case ALIPAY:
                paymentStrategy = "支付宝";
                balance = user.getAliBalance();
                break;
            default:
                paymentStrategy = "微信";
                balance = user.getWechatBalance();
        }
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .price(order.getPrice()).bonusPoints(order.getBonusPoints()).usePoints(true).paymentStrategy(paymentStrategy)
                .balance(balance).consumptionPoints(order.getConsumptionPoints()).leftPoints(user.getMileagePoints())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex))
                .build();
    }
    // TODO: 归还座位
    public void returnSeat(OrderEntity order){
        TrainEntity trainEntity = trainDao.findById(order.getTrainId()).get();
        RouteEntity routeEntity = routeDao.findById(trainEntity.getRouteId()).get();
        List<Long> stations = routeEntity.getStationIds();
        int startStationIdx = stations.indexOf(order.getDepartureStationId()), endStationIdx = stations.indexOf(order.getArrivalStationId());
        if (trainEntity.getTrainType() == TrainType.HIGH_SPEED)
            GSeriesSeatStrategy.INSTANCE.returnSeat(startStationIdx, endStationIdx, order.getSeat(), trainEntity.getSeats());
        else KSeriesSeatStrategy.INSTANCE.returnSeat(startStationIdx, endStationIdx, order.getSeat(), trainEntity.getSeats());
        trainEntity.setUpdatedAt(null); //修改过后勿忘setUpdatedAt(null);
        trainDao.save(trainEntity);
    }
    public void cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        returnSeat(order);
        // TODO: refund money and points
        switch (order.getPaymentStrategy()){
            case ALIPAY:
                new AliPayStrategy().cancel(order);
                break;
            default:
                new WechatStrategy().cancel(order);
        }
        orderDao.save(order);
    }

    //TODO
    public void refundOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();
        if (order.getStatus() != OrderStatus.COMPLETED) throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        // TODO: 归还座位
        returnSeat(order);
        // TODO: refund money and points
        switch (order.getPaymentStrategy()){
            case ALIPAY:
                new AliPayStrategy().refund(user, order);
                break;
            default:
                new WechatStrategy().refund(user, order);
        }
        orderDao.save(order);
        userDao.save(user);

        //测试
        System.err.println("refund");
        if (order.getPaymentStrategy() == ALIPAY)
            System.err.println("ali "+user.getAliBalance()+" "+user.getMileagePoints());
        else System.err.println("wechat "+user.getWechatBalance()+" "+user.getMileagePoints());
    }

    public void payOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }
        // TODO: use payment strategy to pay!
        // TODO: update user's credits, so that user can get discount next time
        //余额不足

        switch (order.getPaymentStrategy()){
            case ALIPAY:
                if (user.getAliBalance() < order.getPrice()) {
                    new AliPayStrategy().cancel(order);
                    returnSeat(order);
                    orderDao.save(order);
                    userDao.save(user);
                    throw new BizException(BizError.ALIPAYBALANCE_NOT_ENOUGH);
                }
                new AliPayStrategy().pay(user, order);
                break;
            default:
                if (user.getWechatBalance() < order.getPrice()) {
                    new WechatStrategy().cancel(order);
                    returnSeat(order);
                    orderDao.save(order);
                    userDao.save(user);
                    throw new BizException(BizError.WECHATBALANCE_NOT_ENOUGH);
                }
                new WechatStrategy().pay(user, order);
                break;
        }
        user.setUpdatedAt(null); //修改过后勿忘setUpdatedAt(null);
        userDao.save(user);
        orderDao.save(order);
        //测试
        System.err.println("pay");
        if (order.getPaymentStrategy() == ALIPAY)
            System.err.println("ali "+user.getAliBalance()+" "+user.getMileagePoints());
        else System.err.println("wechat "+user.getWechatBalance()+" "+user.getMileagePoints());
    }

    //TODO
    public void setUsePoints(Long orderId, boolean usePoints){
        OrderEntity order = orderDao.findById(orderId).get();
        UserEntity user = userDao.findById(order.getUserId()).get();

        order.setUsePoints(usePoints);
        if (usePoints) {
            switch (order.getPaymentStrategy()) {
                case ALIPAY:
                    new AliPayStrategy().usePoints(user, order);
                    break;
                default:
                    new WechatStrategy().usePoints(user, order);
            }
            order.setUpdatedAt(null); //修改过后勿忘setUpdatedAt(null);
            orderDao.save(order);
        }
    }

    //TODO
    public void setPaymentStrategy(Long orderId, String strategyText){
        OrderEntity order = orderDao.findById(orderId).get();
        switch (strategyText) {
            case "支付宝":
                order.setPaymentStrategy(ALIPAY);
                break;
            default:
                order.setPaymentStrategy(WECHAT);
        }
        order.setUpdatedAt(null); //修改过后勿忘setUpdatedAt(null);
        orderDao.save(order);
    }
}
