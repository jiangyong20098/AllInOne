package com.huawei.allinone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 启动自动任务，保证定时任务在后台可以正常运行。
public class AllInOneApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllInOneApplication.class, args);
    }
}
