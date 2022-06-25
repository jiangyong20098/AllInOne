package com.huawei.allinone.guava;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.transform.Result;

@Service
public class GuavaRateLimiterService {
    /*每秒控制5个许可*/
    RateLimiter rateLimiter = RateLimiter.create(5.0);

    /**
     * 获取令牌
     *
     * @return
     */
    public boolean tryAcquire() {
        return rateLimiter.tryAcquire();
    }

}
    @Autowired
    private GuavaRateLimiterService rateLimiterService;

    @ResponseBody
    @RequestMapping("/ratelimiter")
    public Result testRateLimiter() {
        StringUtils.isNotEmpty();
        if (rateLimiterService.tryAcquire()) {
            return ResultUtil.success1(1001, "成功获取许可");
        }
        return ResultUtil.success1(1002, "未获取到许可");
    }