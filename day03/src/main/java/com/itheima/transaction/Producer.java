package com.itheima.transaction;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;

// 发送对象
public class Producer {
    public static void main(String[] args) throws MQClientException, MQBrokerException, RemotingException, InterruptedException {
        // 1.谁来发？
        TransactionMQProducer producer = new TransactionMQProducer("group1");

        // 2.发给谁？
        producer.setNamesrvAddr("localhost:9876");
        // 设置事务监听
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                System.out.println("执行了正常的事务过程。");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                System.out.println("执行事务补偿过程。");

                return LocalTransactionState.UNKNOW;
            }
        });

        producer.start();

        // 3.怎么发？

        // 4.发给谁？
        String msg = "Lichen is a layer.";
        Message message = new Message("topic13", "tag1", msg.getBytes());

        // 发送事务消息
        TransactionSendResult sendResult = producer.sendMessageInTransaction(message, null);

        // 5.发什么？
        System.out.println(sendResult);

    }
}
