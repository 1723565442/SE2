package org.fffd.l23o6.pojo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.util.strategy.payment.PaymentStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

import static org.fffd.l23o6.util.strategy.payment.PaymentStrategy.strategy.ALIPAY;

@Entity
@Table
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long trainId;

    @NotNull
    private Long departureStationId;

    @NotNull
    private Long arrivalStationId;

    @Column
    private double price;
    @Column
    private boolean usePoints;
    @Column
    private double bonusPoints; //奖励积分
    @Column
    private double consumptionPoints; //消耗积分
    @Column
    private PaymentStrategy.strategy paymentStrategy;

    @NotNull
    private OrderStatus status;

    @NotNull
    private String seat;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
