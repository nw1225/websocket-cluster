package com.nw.websocket.broker;

import com.nw.websocket.common.*;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于Redis的消息代理管理器实现类。
 * 该类用于向用户发送消息，可以通过用户ID发送给所有设备，或者发送到指定设备。
 */
@Slf4j
@RequiredArgsConstructor
public class RedisMessageBrokerManage implements MessageBrokerManage {
    private final RedisTemplate<String, String> redisTemplate; // Redis模板，用于Redis操作
    private final ChannelManager channelManager; // 通道管理器，用于管理连接

    /**
     * 向指定用户的所有设备发送消息。
     * 通过Redis获取所有设备的映射，并尝试通过grpc通道发送消息。
     *
     * @param userId  发送消息的用户ID
     * @param message 要发送的消息内容
     */
    @Override
    public void send(String userId, String message) {
        String key = String.format(Constants.clientKeyPrefix, userId, "*");
        ScanOptions scanOptions = ScanOptions.scanOptions().match(key).count(100).build();
        Set<String> collect;
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            collect = cursor.stream().collect(Collectors.toSet());
        }
        List<String> values = redisTemplate.opsForValue().multiGet(collect);
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        log.debug("userId:{} node:{}", userId, values);
        for (String nodeId : values) {
            Channel channel = channelManager.getChannel(nodeId);
            if (Objects.isNull(channel)) {
                continue;
            }
            push(channel, new Message(userId, message));

        }
    }

    private void push(Channel channel, Message message) {
        MessageServiceGrpc.MessageServiceStub stub = MessageServiceGrpc.newStub(channel);
        StreamObserver<MessageServiceProto.Message> requestObserver = stub.push(new NoopStreamObserver<>());
        MessageServiceProto.Message request = MessageServiceProto.Message.newBuilder()
                .setDevice(message.getDevice())
                .setUserId(message.getUserId())
                .setMessage(message.getMessage())
                .build();
        requestObserver.onNext(request);
    }

    /**
     * 向指定用户和指定设备发送消息。
     * 直接通过Redis获取指定设备的映射，并尝试通过grpc通道发送消息。
     *
     * @param userId  发送消息的用户ID
     * @param device  指定的设备
     * @param message 要发送的消息内容
     */
    @Override
    public void send(String userId, String device, String message) {
        String key = String.format(Constants.clientKeyPrefix, userId, device);
        String nodeId = redisTemplate.opsForValue().get(key);
        log.debug("userId:{} node:{}", userId, nodeId);
        Channel channel = channelManager.getChannel(nodeId);
        if (Objects.isNull(channel)) {
            return;
        }
        push(channel, new Message(userId, message).setDevice(device));
    }
}
