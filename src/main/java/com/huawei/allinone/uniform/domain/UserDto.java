package com.huawei.allinone.uniform.domain;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserDto {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空！")
    private String username;
    /**
     * 性别
     */
    @NotBlank(message = "性别不能为空！")
    private String gender;
    /**
     * 年龄
     */
    @Min(value = 1, message = "年龄有误！")
    @Max(value = 120, message = "年龄有误！")
    private int age;
    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空！")
    private String address;
    /**
     * 邮箱
     */
    @Email(message = "邮箱有误！")
    private String email;
    /**
     * 手机号码
     */
    @Pattern(regexp = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$",message = "手机号码有误！")
    private String mobile;
}