package org.fffd.l23o6.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.StationEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.TrainSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final OrderDao orderDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        // TODO
        List<TrainEntity> trains = trainDao.findByDate(date);
        List<TrainVO> ret = new ArrayList<>();
        for (TrainEntity trainEntity : trains) {
            RouteEntity routeEntity = routeDao.findById(trainEntity.getRouteId()).get();
            List<Long> stations = routeEntity.getStationIds();
            int startStationIdx = stations.indexOf(startStationId), endStationIdx = stations.indexOf(endStationId);
            if (startStationIdx == -1 || endStationIdx == -1) continue; //没有起始或终点站
            if (startStationIdx < endStationIdx) {  //起始站->终点站
                //获取座席
                List<TicketInfo> ticketInfos = trainEntity.getTrainType() == TrainType.HIGH_SPEED
                        ?GSeriesSeatStrategy.INSTANCE.getTicketInfos(startStationIdx, endStationIdx, trainEntity.getSeats())
                        :KSeriesSeatStrategy.INSTANCE.getTicketInfos(startStationIdx, endStationIdx, trainEntity.getSeats());
                ret.add(TrainVO.builder().id(trainEntity.getId()).name(trainEntity.getName()).trainType(trainEntity.getTrainType().getText())
                        .startStationId(startStationId).endStationId(endStationId).departureTime(trainEntity.getDepartureTimes().get(startStationIdx))
                        .arrivalTime(trainEntity.getArrivalTimes().get(endStationIdx)).ticketInfo(ticketInfos).build());
            }
        }
        return ret;
    }


    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TrainMapper.INSTANCE::toAdminTrainVO).collect(Collectors.toList());
    }


    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                         List<Date> departureTimes) {
        // 火车存在
        trainExisted(name, -1L);
        // 检测命名 时间日期是否正确
        validateTrain(name, routeId, type, date, arrivalTimes, departureTimes);
        // 正确以后添加
        add(routeId, name, type, date, arrivalTimes, departureTimes);
    }


    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                            List<Date> departureTimes) {
        // 火车被订单引用
        trainIsUsed(id);
        // 火车已存在
        trainExisted(name, id);
        // 命名不规范
        validateTrain(name, routeId, type, date, arrivalTimes, departureTimes);
        // 全合法 可以修改
        TrainEntity train = trainDao.findById(id).get();
        RouteEntity route = routeDao.findById(routeId).get();
        train.setName(name);
        train.setRouteId(routeId);
        train.setTrainType(type);
        train.setDate(date);
        train.setArrivalTimes(arrivalTimes);
        train.setDepartureTimes(departureTimes);
        train.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                train.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                train.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(train);
    }


    private void trainExisted(String name,Long id){
        TrainEntity entity = trainDao.findByName(name);
        if(entity!=null && !entity.getId().equals(id)) throw new BizException(BizError.TRAIN_EXISTS);
    }

    public void trainIsUsed(Long id){
        List<OrderEntity> orders = orderDao.findAll();
        for (OrderEntity order : orders)
            if (order.getTrainId().equals(id))
                throw new BizException(BizError.TRAIN_CAN_NOT_DELETE);
    }

    @Override
    public void deleteTrain(Long id) {
        // 已有订单包含该火车
       trainIsUsed(id);
        trainDao.deleteById(id);
    }


    private void validateTrain(String name, Long routeId,TrainType type, String date ,List<Date> arrivalTimes,
                               List<Date> departureTimes){
        //火车名不规范
        if (!(name.substring(1).matches("\\d{3}") && (name.charAt(0) == 'G' || name.charAt(0) == 'K')))
            throw new BizException(BizError.TRAIN_NAME_NOT_VALID);
        if ((name.charAt(0) == 'G' && type != TrainType.HIGH_SPEED)||(name.charAt(0) == 'K' && type != TrainType.NORMAL_SPEED))
            throw new BizException(BizError.TRAIN_NAME_NOT_MATCH_TYPE);
        //火车经停站时间表长度不一致
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != arrivalTimes.size() || route.getStationIds().size() != departureTimes.size())
            throw new BizException(BizError.TRAIN_TIMES_SIZE_NOT_SAME);
        //火车经停站时间表不完整
        for (int i = 0; i < arrivalTimes.size(); i++)
            if (arrivalTimes.get(i) == null || departureTimes.get(i) == null)
                throw new BizException(BizError.TRAIN_TIMES_NOT_COMPLETED);
        //火车经停站时间表不合法
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date departuretime = null;
        try {
            departuretime = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = departuretime.getTime();
        Date add1 = new Date();
        add1.setTime(time+1000*60*60*24);
        if((departuretime.compareTo(departureTimes.get(0))>0)||(add1.compareTo(departureTimes.get(0)))<=0)
            throw new BizException(BizError.TRAIN_TIMES_NOT_VALID);
        if (arrivalTimes.get(arrivalTimes.size() - 1).compareTo(departureTimes.get(arrivalTimes.size() - 2)) <= 0)
            throw new BizException(BizError.TRAIN_TIMES_NOT_VALID);
        for (int i = 1; i < arrivalTimes.size()-1; i++) {
            if (arrivalTimes.get(i).compareTo(departureTimes.get(i)) >= 0
                    ||arrivalTimes.get(i).compareTo(departureTimes.get(i-1)) <= 0)
                throw new BizException(BizError.TRAIN_TIMES_NOT_VALID);
        }
    }

    private void add(Long routeId, String name, TrainType type, String date, List<Date> arrivalTimes,
                     List<Date> departureTimes){
        RouteEntity route = routeDao.findById(routeId).get();
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(entity);
    }
}
