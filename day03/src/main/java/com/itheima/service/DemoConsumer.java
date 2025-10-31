package com.itheima.service;

import com.itheima.domain.User;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "topic1",consumerGroup = "group1")
public class DemoConsumer implements RocketMQListener<User> {

    @Override
    public void onMessage(User user) {
        System.out.println(user);
    }

    public void onMessage(String msg) {
        System.out.println(msg);
    }

}
