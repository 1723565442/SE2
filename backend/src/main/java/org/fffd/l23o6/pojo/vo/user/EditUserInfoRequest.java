package org.fffd.l23o6.pojo.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "修改用户信息请求")
public class EditUserInfoRequest {

    @Schema(description = "姓名", required = true)
    @NotNull
    @Size(min = 2, max = 16, message = "姓名长度必须在 2-16 之间")
    @Pattern.List({
            @Pattern(regexp = "^[\\u4E00-\\u9FA5]{2,16}$", message = "姓名只能包含中文"),
    })
    private String name;

    @Schema(description = "证件号", required = true)
    @NotNull
    @Size(min = 18, max = 18, message = "证件号长度必须为18")
    @Pattern.List({
            @Pattern(regexp = "^\\d{17}[0-9X]$", message = "证件号格式错误"),
    })
    private String idn;

    @Schema(description = "手机号", required = true)
    @NotNull
    @Size(min = 11, max = 11, message = "手机号长度必须为11")
    @Pattern.List({
            @Pattern(regexp = "^\\d{11}$", message = "手机号格式错误"),
    })
    private String phone;

    @Schema(description = "证件类型", required = true)
    @NotNull
    @Pattern.List({
            @Pattern(regexp = "^身份证|护照|其他$", message = "证件类型错误"),
    })
    private String type;

    @Schema(description = "用户身份", required = true)
    @NotNull
    @Pattern.List({
            @Pattern(regexp = "^用户|管理员$", message = "用户类型错误"),
    })
    private String identity;

    @Schema(description = "里程积分", required = true)
    @NotNull
    @Digits(integer = Integer.MAX_VALUE, fraction = Integer.MAX_VALUE, message = "积分必须为数字")
    @DecimalMin(value = "0.0", message = "积分必须大于或等于0")
    private double mileagePoints;

    @Schema(description = "支付宝余额", required = true)
    @NotNull
    @Digits(integer = Integer.MAX_VALUE, fraction = Integer.MAX_VALUE, message = "支付宝余额必须为数字")
    @DecimalMin(value = "0.0", message = "支付宝余额必须大于或等于0")
    private double aliBalance;

    @Schema(description = "微信余额", required = true)
    @NotNull
    @Digits(integer = Integer.MAX_VALUE, fraction = Integer.MAX_VALUE, message = "微信余额必须为数字")
    @DecimalMin(value = "0.0", message = "微信余额必须大于或等于0")
    private double wechatBalance;

}
