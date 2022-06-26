package com.huawei.allinone.guava.controller;

import com.huawei.allinone.common.RespBean;
import com.huawei.allinone.guava.service.GuavaRateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guava")
public class GuavaController {
    @Autowired
    private GuavaRateLimiterService rateLimiterService;

    @ResponseBody
    @RequestMapping("/rate-limiter")
    public RespBean testRateLimiter() {
        if (rateLimiterService.tryAcquire()) {
            return RespBean.ok("1001", "成功获取许可");
        }
        return RespBean.error("1002", "未获取到许可");
    }
}
