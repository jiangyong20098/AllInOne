package com.huawei.allinone.uniform.exception;

import com.huawei.allinone.uniform.response.AppCode;
import com.huawei.allinone.uniform.response.StatusCode;
import lombok.Getter;

/**
 * 自定义API异常
 */
@Getter
public class ApiException extends RuntimeException {
    private int code;
    private String msg;

    // 手动设置异常
    public ApiException(StatusCode statusCode, String message) {
        // message用于用户设置抛出错误详情，例如：当前价格-5，小于0
        super(message);
        // 状态码
        this.code = statusCode.getCode();
        // 状态码配套的msg
        this.msg = statusCode.getMsg();
    }

    // 默认异常使用APP_ERROR状态码
    public ApiException(String message) {
        super(message);
        this.code = AppCode.APP_ERROR.getCode();
        this.msg = AppCode.APP_ERROR.getMsg();
    }
}
