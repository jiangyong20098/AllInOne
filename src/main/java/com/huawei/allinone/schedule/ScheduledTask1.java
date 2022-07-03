package com.huawei.allinone.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试@Scheduled定时任务，从配置文件读取信息
 *
 * @EnableScheduling 这个一定要加上；否则，不会定时启动任务！
 *
 * @Scheduled中的参数说明：
 *
 * @Scheduled(fixedRate=2000)：上一次开始执行时间点后2秒再次执行；
 *
 * @Scheduled(fixedDelay=2000)：上一次执行完毕时间点后2秒再次执行；
 *
 * @Scheduled(initialDelay=1000, fixedDelay=2000)：第一次延迟1秒执行，然后在上一次执行完毕时间点后2秒再次执行；
 *
 * @Scheduled(cron="* * * * * ?")：按cron规则执行。
 *
 * 在线Cron表达式生成器：http://cron.qqe2.com/
 *
 * 任务都是在同一个线程中执行
 * 2022-07-03 14:21:05.144  INFO 5148 --- [   scheduling-1] c.h.allinone.schedule.ScheduledTask1     : 任务2，从配置文件加载任务信息，当前时间: 14:21:05
 * 2022-07-03 14:21:05.145  INFO 5148 --- [   scheduling-1] c.h.allinone.schedule.ScheduledTask1     : 任务1，从配置文件加载任务信息，当前时间: 14:21:05
 */
@Component
@Slf4j
public class ScheduledTask1 {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedDelayString = "${jobs.fixedDelay}")
    public void getTask1() {
        log.info("任务1，从配置文件加载任务信息，当前时间: {}", dateFormat.format(new Date()));
    }

    @Scheduled(cron = "${jobs.cron}")
    public void getTask2() {
        log.info("任务2，从配置文件加载任务信息，当前时间: {}", dateFormat.format(new Date()));
    }
}
