package org.fffd.l23o6.pojo.vo.user;

import lombok.Data;

@Data
public class UserVO {
    private String identity;
    private String username;
    private String name;
    private String phone;
    private String idn;
    private String type;
    private double mileagePoints;
    private double aliBalance;
    private double wechatBalance;
}
