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

@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {
    private final MessageBrokerManage messageBrokerManage;

    @KafkaListener(topics = "${websocket.cluster.topic}")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.debug("im message key:{} msg:{} partition:{}", record.key(), record.value(), record.partition());
        Message message = JSON.parseObject(record.value(), Message.class);
        if (Objects.nonNull(message)) {
            String device = message.getDevice();
            if (StringUtils.hasLength(device)) {
                messageBrokerManage.send(message.getUserId(), message.getDevice(), message.getMessage());
            } else {
                messageBrokerManage.send(message.getUserId(), message.getMessage());
            }
        }
        ack.acknowledge();
    }
}
