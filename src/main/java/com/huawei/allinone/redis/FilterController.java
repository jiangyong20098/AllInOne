package com.huawei.allinone.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.IOException;

@RestController
@Slf4j
public class FilterController {

    @GetMapping("/test/{userId}")
    public void checkSensitiveWord(@PathVariable("userId") String userId, @PathVariable("searchKey") String searchKey) throws IOException {
        //非法敏感词汇判断
        SensitiveFilter filter = SensitiveFilter.getInstance();
        int n = filter.CheckSensitiveWord(searchKey,0,1);
        if(n > 0){ //存在非法字符
            log.info("这个人输入了非法字符--> {},不知道他到底要查什么~ userid--> {}",searchKey,userId);
            return;
        }
        log.info("searchKey: {}", searchKey);
    }


    @GetMapping("/replace/{userId}")
    public void replaceSensitiveWord(@PathVariable("userId") String userId, @PathParam("text") String text) throws IOException {
        log.info("text: {}", text);
        SensitiveFilter filter = SensitiveFilter.getInstance();
        String result = filter.replaceSensitiveWord(text, 1, "*");
        log.info("result: {}", result);
    }
}
