package org.fffd.l23o6.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.fffd.l23o6.pojo.entity.UserEntity;

public interface UserService {
    void login(String username, String password);
    void register(String username, String password, String name, String idn, String phone, String type, String identity);

    UserEntity findByUserName(String username);
    void editInfo(String username, String name, String idn, String phone, String type, String identity, double mileagePoints, double aliBalance, double wechatBalance);
}