package com.huawei.allinone.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * kafka消费者
 *
 * @since 2022-06-11
 */
@Component
public class KafkaConsumer {
    // kafka的监听器，topic为"testTopic"，消费者组为"testGroup"
    @KafkaListener(topics = "testTopic", groupId = "testGroup")
    @SendTo("topic-a")  // 将此消息转发给主题topic-a
    public String listenGroup1(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String value = record.value();
        System.out.println("recive msg = " + value);
        System.out.println("record = " + record);

        //手动提交offset
        ack.acknowledge();
        return "转发消息：" + value;
    }

    // 配置多个消费组
    @KafkaListener(topics = "testTopic", groupId = "testGroup")
    public void listenGroup2(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String value = record.value();
        System.out.println(value);
        System.out.println(record);
        ack.acknowledge();
    }

    @KafkaListener(topics = "topic-a")
    public void listenTopicA(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        System.out.println("topic-a record = " + record);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "xxxxx")
    public void listenTopicXxxxx(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        System.out.println("xxxxx record = " + record);
        acknowledgment.acknowledge();
    }

    /**
     * 消费者监听器生命周期控制
     * 消费者监听器有三个生命周期：启动、停止、继续；如果我们想控制消费者监听器生命周期，
     * 需要修改@KafkaListener 的 autoStartup 属性为false，并给监听器 id 属性赋值
     * 然后通过KafkaListenerEndpointRegistry 控制id 对应的监听器的启动停止继续
     * @param record
     */
    @KafkaListener( id = "listener1",topics = "test",autoStartup ="false" )
    public void testStart(ConsumerRecord<?, String> record){
        System.out.println(record.value());
    }

    @KafkaListener(topics = "topic-return")
    @SendTo
    public String listen(String message) {
        return "consumer return:".concat(message);
    }
}