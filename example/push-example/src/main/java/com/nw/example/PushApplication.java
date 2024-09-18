package com.nw.example;

import com.nw.im.common.Message;
import com.nw.im.push.MessagePushService;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping
@SpringBootApplication
public class PushApplication {
    @Resource
    private MessagePushService messagePushService;

    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class, args);
    }

    @GetMapping
    public void test(@RequestParam String userId) throws ExecutionException, InterruptedException {
        messagePushService.send(new Message(userId, UUID.randomUUID().toString()));
    }
}
