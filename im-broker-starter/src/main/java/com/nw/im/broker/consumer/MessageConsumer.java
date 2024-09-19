package com.nw.im.broker.consumer;

import com.alibaba.fastjson.JSON;
import com.nw.im.broker.MessageBrokerManage;
import com.nw.im.common.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 消息消费者，用于监听Kafka中的消息并处理
 */
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {
    // 注入消息代理管理器，用于发送消息
    private final MessageBrokerManage messageBrokerManage;

    /**
     * 监听Kafka主题中的消息
     *
     * @param record 消费者记录，包含消息的键和值
     * @param ack    用于确认消息处理的凭证
     */
    @KafkaListener(topics = "${websocket.cluster.topic}")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        // 记录日志，包括消息的键、值和分区信息
        log.debug("im message key:{} msg:{} partition:{}", record.key(), record.value(), record.partition());
        // 将JSON字符串解析为Message对象
        Message message = JSON.parseObject(record.value(), Message.class);
        // 如果消息对象非空
        if (Objects.nonNull(message)) {
            String device = message.getDevice();
            // 如果设备信息非空且有长度，则根据用户ID、设备信息发送消息
            if (StringUtils.hasLength(device)) {
                messageBrokerManage.send(message.getUserId(), message.getDevice(), message.getMessage());
            } else {
                // 如果设备信息为空，根据用户ID发送消息到所有设备
                messageBrokerManage.send(message.getUserId(), message.getMessage());
            }
        }
        // 确认消息已处理
        ack.acknowledge();
    }
}
