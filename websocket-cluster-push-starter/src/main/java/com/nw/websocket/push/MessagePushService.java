package com.nw.websocket.push;

import com.alibaba.fastjson.JSON;
import com.nw.websocket.common.Message;
import com.nw.websocket.push.config.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutionException;

/**
 * 消息推送服务类，负责通过Kafka将消息发送到指定的主题
 */
@Slf4j
@RequiredArgsConstructor
public class MessagePushService {
    // Websocket配置属性，包括Kafka主题等信息
    private final WebsocketProperties websocketProperties;
    // Kafka模板，用于发送消息到Kafka主题
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息到Kafka主题
     *
     * @param message 待发送的消息对象
     * @throws ExecutionException   如果异步发送操作抛出异常
     * @throws InterruptedException 如果当前线程在等待发送操作完成时被中断
     */
    public void send(Message message) throws ExecutionException, InterruptedException {
        // 将消息转换为JSON字符串，然后通过Kafka模板发送到指定的主题，使用用户ID作为键，确保消息的路由
        kafkaTemplate.send(websocketProperties.getTopic(), message.getUserId(), JSON.toJSONString(message)).get();
    }
}
