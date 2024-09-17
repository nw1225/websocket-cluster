package com.nw.example;

import com.nw.im.broker.MessageBrokerManage;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
@SpringBootApplication
public class BrokerApplication {
    @Resource
    private MessageBrokerManage routerManage;

    public static void main(String[] args) {
        SpringApplication.run(BrokerApplication.class, args);
    }

    @GetMapping
    public void test(@RequestParam String userId) {
        routerManage.send(userId,UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
}
