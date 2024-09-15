package com.nw.im.push;

import com.nw.im.push.config.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutionException;


@RequiredArgsConstructor
public class MessagePushService {
    private final WebsocketProperties websocketProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <V> void send(V message) throws ExecutionException, InterruptedException {
        // todo
        kafkaTemplate.send(websocketProperties.getTopic(), message).get();
    }
}
