package org.fffd.l23o6.util.strategy.payment;

import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.UserEntity;

public abstract class PaymentStrategy {
    // TODO: implement this by adding necessary methods and implement specified strategy
    public enum strategy{ALIPAY, WECHAT}
    protected double[] MileagePoints = {0, 1000, 3000, 10000, 50000, Double.MAX_VALUE};
    protected double[] discounts = {0, 0.001, 0.0015, 0.002, 0.0025, 0.003};
    protected double[] preCounts = {0, 0, 1, 4, 18, 118};
    public abstract void refund(UserEntity user, OrderEntity order);
    public abstract void cancel(OrderEntity order);
    public abstract void usePoints(UserEntity user, OrderEntity order);
    public abstract void pay(UserEntity user, OrderEntity order);
}
