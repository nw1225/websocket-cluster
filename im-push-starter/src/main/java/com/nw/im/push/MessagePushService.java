package com.nw.im.push;

import com.alibaba.fastjson.JSON;
import com.nw.im.common.Message;
import com.nw.im.push.config.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
public class MessagePushService {
    private final WebsocketProperties websocketProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(Message message) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(websocketProperties.getTopic(), message.getUserId(), JSON.toJSONString(message)).get();
    }
}
