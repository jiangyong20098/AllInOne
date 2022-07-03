package com.huawei.allinone.uniform.exception;

import com.huawei.allinone.uniform.response.ResultCode;
import com.huawei.allinone.uniform.response.ResultVo;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常增强
 */
@RestControllerAdvice
public class ControllerExceptionAdvice {
    /**
     * 处理BindException
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler({BindException.class})
    public ResultVo MethodArgumentNotValidExceptionHandler(BindException e) {
        // 从异常对象中拿到ObjectError对象
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return new ResultVo(ResultCode.VALIDATE_ERROR, objectError.getDefaultMessage());
    }

    /**
     * 处理API异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(ApiException.class)
    public ResultVo ApiExceptionHandler(ApiException e) {
        // log.error(e.getMessage(), e); 由于还没集成日志框架，暂且放着，写上TODO
        return new ResultVo(e.getCode(), e.getMsg(), e.getMessage());
    }

}