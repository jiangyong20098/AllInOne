package com.huawei.allinone.guava;

import com.simons.cn.springbootdemo.aspect.RateLimitAspect;
import com.simons.cn.springbootdemo.util.ResultUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
 
/**
 * 类描述：RateLimit限流测试（基于 注解+切面 方式）
 * 创建人：simonsfan
 */
@Controller
public class TestController {
 
    @ResponseBody
    @RateLimitAspect         //可以非常方便的通过这个注解来实现限流
    @RequestMapping("/test")
    public String test(){
        return ResultUtil.success1(1001, "success").toString();
    }