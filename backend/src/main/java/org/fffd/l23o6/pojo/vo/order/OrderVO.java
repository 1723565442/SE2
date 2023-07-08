package org.fffd.l23o6.pojo.vo.order;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
public class OrderVO {
    private Long id;
    private Long trainId;
    private Long startStationId;
    private Long endStationId;
    private Date departureTime;
    private Date arrivalTime;
    private String status;
    private Date createdAt;
    private String seat;
    private double price;
    private double bonusPoints;
    private double consumptionPoints;
    private double leftPoints;
    private boolean usePoints;
    private double balance;
    private String paymentStrategy;
}
