package cn.itnanls.filter;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class Consumer {
    public static void main(String[] args) throws Exception {
        // 谁来收
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");

        // 从哪里收
        consumer.setNamesrvAddr("localhost:9876");

        // 监听某个消息队列
        // consumer.subscribe("topic1","tag1 || vip");

        // 消费者 sql过滤
        consumer.subscribe("topic1", MessageSelector.bySql("age > 16 and name = 'zhangsan"));

        // 处理业务流程，注册监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : list) {
                    System.out.println("msg = " + msg);
                    byte[] body = msg.getBody();
                    System.out.println(new String(body));
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();

        System.out.println("Consumer is successful to start.");

        // 消费者长连接，持续接受信息
        // consumer.shutdown();

    }
}
