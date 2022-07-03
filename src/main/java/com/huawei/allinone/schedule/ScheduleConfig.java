package com.huawei.allinone.schedule;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 * 多线程执行定时任务
 *
 * 所有定时任务都放到一个线程池中，定时任务启动时使用不同的线程执行
 *
 * 从ScheduledAnnotationBeanPostProcessor类开始一路找下去。果然，在ScheduledTaskRegistrar（定时任务注册类）
 * 中的ScheduleTasks中有这样一段判断：
 *     public ScheduledAnnotationBeanPostProcessor() {
 *         this.registrar = new ScheduledTaskRegistrar();
 *     }
 *
 *     ScheduledTaskRegistrar.java
 *     public void afterPropertiesSet() {
 *         this.scheduleTasks();
 *     }
 *
 *     protected void scheduleTasks() {
 *         if (this.taskScheduler == null) {
 *             this.localExecutor = Executors.newSingleThreadScheduledExecutor();
 *             this.taskScheduler = new ConcurrentTaskScheduler(this.localExecutor);
 *         }
 *         ......
 *     }
 * 这就说明如果taskScheduler为空，那么就给定时任务做了一个单线程的线程池，正好在这个类中还有一个设置taskScheduler的方法：
 *
 * public void setScheduler(@Nullable Object scheduler) {
 *         if (scheduler == null) {
 *             this.taskScheduler = null;
 *         } else if (scheduler instanceof TaskScheduler) {
 *             this.taskScheduler = (TaskScheduler)scheduler;
 *         } else {
 *             if (!(scheduler instanceof ScheduledExecutorService)) {
 *                 throw new IllegalArgumentException("Unsupported scheduler type: " + scheduler.getClass());
 *             }
 *
 *             this.taskScheduler = new ConcurrentTaskScheduler((ScheduledExecutorService)scheduler);
 *         }
 *
 *     }
 *
 * 这样问题就很简单了，我们只需用调用这个方法显式的设置一个ScheduledExecutorService就可以达到并发的效果了。我们要做的仅仅
 * 是实现SchedulingConfigurer接口，重写configureTasks方法就OK了；
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 设置一个长度为10的定时任务线程池
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(10));
    }
}

/**
 * 2022-07-03 14:58:41.472  INFO 6708 --- [pool-1-thread-8] c.h.allinone.schedule.ScheduledTask2     : ===fixedRate: 第7次执行方法
 * 2022-07-03 14:58:41.507  INFO 6708 --- [ool-1-thread-10] c.h.allinone.schedule.ScheduledTask1     : 任务1，从配置文件加载任务信息，当前时间: 14:58:41
 * 2022-07-03 14:58:41.506  INFO 6708 --- [pool-1-thread-9] c.h.allinone.schedule.ScheduledTask2     : ===fixedDelay: 第7次执行方法
 * 2022-07-03 14:58:42.469  INFO 6708 --- [pool-1-thread-2] c.h.allinone.schedule.ScheduledTask2     : ===initialDelay: 第7次执行方法
 */
