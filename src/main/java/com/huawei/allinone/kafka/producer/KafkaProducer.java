package com.huawei.allinone.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * kafka生产者【实际上就是一个Controller，用来进行消息生产】
 *
 * @since 2022-06-11
 */
@RestController
public class KafkaProducer {
    private final static String TOPIC_NAME = "testTopic"; // topic的名称

    @Autowired
    KafkaListenerEndpointRegistry listenerRegistry;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping("/send/{user}")
    public void send(@PathVariable("user") String user) {
        //发送功能就一行代码~
        kafkaTemplate.send(TOPIC_NAME, "key", user + " test message send~");
    }

    /**
     * @Scheduled(initialDelay = 3000, fixedRate = 5000)  // 第一次延迟3秒后执行，之后按fixedRate的规则每 5秒执行一次
     * Cron表达式是一个字符串，字符串以5或6个空格隔开，分为6或7个域，每一个域代表一个含义，Cron有如下两种语法格式：     *
     * Seconds Minutes Hours DayofMonth Month DayofWeek Year
     * 或
     * Seconds Minutes Hours DayofMonth Month DayofWeek     *
     * 一个cron表达式有至少6个（也可能7个）有空格分隔的时间元素。
     * 按顺序依次为
     * 秒（0~59）
     * 分钟（0~59）
     * 小时（0~23）
     * 天（月）（0~31，但是你需要考虑你月的天数）
     * 月（0~11）
     * 天（星期）（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
     * 年份（1970－2099)
     * <p>
     * 0 0 10,14,16 * * ? 每天上午10点，下午2点，4点
     * 0 0/30 9-17 * * ? 朝九晚五工作时间内每半小时
     * 0 0 12 ? * WED 表示每个星期三中午12点
     * "0 0 12 * * ?" 每天中午12点触发
     * "0 15 10 ? * *" 每天上午10:15触发
     * "0 15 10 * * ?" 每天上午10:15触发
     * "0 15 10 * * ? *" 每天上午10:15触发
     * "0 15 10 * * ? 2005" 2005年的每天上午10:15触发
     * "0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
     * "0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
     * "0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
     * "0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
     * "0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
     * "0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
     * "0 15 10 15 * ?" 每月15日上午10:15触发
     * "0 15 10 L * ?" 每月最后一日的上午10:15触发
     * "0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
     * "0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
     * "0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
     */
    @Scheduled(cron = "*/5 * * * * ?")
    @Transactional
    public void producerTest() {
        kafkaTemplate.send("topic-a", "xxxxxxxxxxxxxx");
    }

    @Scheduled(cron = "*/15 * * * * ?")
    @Transactional
    public void testListener() {
        int i = 20;
        if (i == 20) {
            listenerRegistry.getListenerContainer("listener1").start();
        }
        System.out.println("生产者生产消息" + i++);
        kafkaTemplate.send("test", "xxx" + i);
    }

    /**
     * 指定回调函数
     */
    @Scheduled(cron = "*/15 * * * * ?")
    @Transactional
    public void send() {
        ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send("xxxxx", "test");
        send.addCallback(new ListenableFutureCallback() {
            @Override
            public void onSuccess(Object o) {
                System.out.println("消息发送成功");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("消息发送失败");
            }
        });
    }

    /**
     * 发送消息并调get阻塞(事务场景使用)
     */
    @Scheduled(cron = "*/15 * * * * ?")
    @Transactional
    public void send1() {
        try {
            kafkaTemplate.send("xxxxx", "test").get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    //@Scheduled(cron = "*/1 * * * * ?")
    /*@Transactional
    public void returnTestProducer(){
        ProducerRecord<String, String> record = new ProducerRecord<>("topic-return", "test-return");
        RequestReplyFuture<String, String, String> replyFuture = replyingTemplate.sendAndReceive(record);
        try {
            String value = replyFuture.get().value();
            System.out.println(value);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }*/
}