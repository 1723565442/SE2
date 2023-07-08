package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;

//TODO
public class WechatStrategy extends PaymentStrategy{
    @Override
    public void refund(UserEntity user, OrderEntity order) {
        user.setWechatBalance(user.getWechatBalance() + order.getPrice());
        user.setMileagePoints(user.getMileagePoints() - order.getBonusPoints() + order.getConsumptionPoints());
        order.setBonusPoints(0);
        order.setConsumptionPoints(0);
        order.setStatus(OrderStatus.REFUNDED);
    }

    @Override
    public void cancel(OrderEntity order) {
        order.setBonusPoints(0);
        order.setConsumptionPoints(0);
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public void usePoints(UserEntity user, OrderEntity order){
        double discount = 0, price = order.getPrice();
        double consumptionPoints = 0, points = user.getMileagePoints();
        for (int i = 0; i < MileagePoints.length; i++) {
            if (preCounts[i] >= price){ //整积分可抵扣所全部价钱
                consumptionPoints = MileagePoints[i-1];
                discount = price;
                break;
            }
            if (points < MileagePoints[i]){
                consumptionPoints = points;
                discount = preCounts[i] + (points - MileagePoints[i-1]) * discounts[i];
                if (discount >= price){ //整+部分散积分可抵扣所全部价钱
                    consumptionPoints = points - (discount-price)/discounts[i];
                    discount = price;
                }
                break;
            }
        }
        order.setConsumptionPoints(consumptionPoints);
        order.setPrice(price - discount);
    }

    @Override
    public void pay(UserEntity user, OrderEntity order) {
        user.setMileagePoints(user.getMileagePoints() + order.getBonusPoints() - order.getConsumptionPoints());
        user.setWechatBalance(user.getWechatBalance() - order.getPrice());
        order.setStatus(OrderStatus.COMPLETED);
    }
}
