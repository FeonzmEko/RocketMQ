package cn.itnanls.one2many;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

// 发送对象
public class Producer {
    public static void main(String[] args) throws MQClientException, MQBrokerException, RemotingException, InterruptedException {
        // 1.谁来发？
        DefaultMQProducer producer = new DefaultMQProducer("group1");

        // 2.发给谁？
        producer.setNamesrvAddr("localhost:9876");
        producer.start();

        // 3.怎么发？

        // 4.发给谁？
        for (int i = 0; i < 10; i++) {
            String msg = "Lichen is a layer." + i;
            Message message = new Message("topic3", "tag1", msg.getBytes());
            SendResult sendResult = producer.send(message);

            // 5.发什么？
            System.out.println(sendResult);
        }


        // 6.打扫战场
        producer.shutdown();

    }
}
