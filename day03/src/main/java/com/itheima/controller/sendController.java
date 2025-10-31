package com.itheima.controller;

import com.itheima.domain.User;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class sendController {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/send")
    public String send(){
        // 发送消息
        String msg = "Lc is a big layer,so I hate her.";
        //rocketMQTemplate.convertAndSend("topic1",msg);
        rocketMQTemplate.convertAndSend("topic1",new User("lc",23));

        return "success";
    }
}
