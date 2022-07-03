package com.huawei.allinone.uniform.response;


import com.huawei.allinone.uniform.response.StatusCode;
import lombok.Getter;

/**
 * 统一响应码
 */
@Getter
public enum AppCode implements StatusCode {
    APP_ERROR(2000, "业务异常"),
    PRICE_ERROR(2001, "价格异常"),
    ORDER_NOT_EXIST(2002, "订单号不存在");

    private int code;
    private String msg;

    AppCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
