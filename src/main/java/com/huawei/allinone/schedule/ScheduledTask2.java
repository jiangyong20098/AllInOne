package com.huawei.allinone.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 使用 @Scheduled来创建定时任务 这个注解用来标注一个定时任务方法。
 *
 * 通过看 @Scheduled源码可以看出它支持多种参数：
 *
 * cron：cron表达式，指定任务在特定时间执行；
 *
 * fixedDelay：表示上一次任务执行完成后多久再次执行，参数类型为long，单位ms；
 *
 * fixedDelayString：与fixedDelay含义一样，只是参数类型变为String；
 *
 * fixedRate：表示按一定的频率执行任务，参数类型为long，单位ms；
 *
 * fixedRateString: 与fixedRate的含义一样，只是将参数类型变为String；
 *
 * initialDelay：表示延迟多久再第一次执行任务，参数类型为long，单位ms；
 *
 * initialDelayString：与initialDelay的含义一样，只是将参数类型变为String；
 *
 * zone：时区，默认为当前时区，一般没有用到。
 */
@Component
@Slf4j
public class ScheduledTask2 {
    private int fixedDelayCount = 1;
    private int fixedRateCount = 1;
    private int initialDelayCount = 1;
    private int cronCount = 1;

    /**
     * fixedDelay = 5000表示当前方法执行完毕5000ms后，Spring scheduling会再次调用该方法
     */
    @Scheduled(fixedDelay = 5000)
    public void testFixDelay() {
        log.info("===fixedDelay: 第{}次执行方法", fixedDelayCount++);
    }

    /**
     * fixedRate = 5000表示当前方法开始执行5000ms后，Spring scheduling会再次调用该方法
     */
    @Scheduled(fixedRate = 5000)
    public void testFixedRate() {
        log.info("===fixedRate: 第{}次执行方法", fixedRateCount++);
    }

    /**
     * initialDelay = 1000表示延迟1000ms执行第一次任务
     */
    @Scheduled(initialDelay = 1000, fixedRate = 5000)
    public void testInitialDelay() {
        log.info("===initialDelay: 第{}次执行方法", initialDelayCount++);
    }

    /**
     * cron接受cron表达式，根据cron表达式确定定时规则
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void testCron() {
        log.info("===initialDelay: 第{}次执行方法", cronCount++);
    }
}
