package com.nw.im.broker;

import com.nw.im.broker.tcp.TcpChannelManager;
import com.nw.im.common.Constants;
import com.nw.im.common.Message;
import io.netty.channel.Channel;
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
    private final TcpChannelManager tcpChannelManager; // TCP通道管理器，用于管理TCP连接

    /**
     * 向指定用户的所有设备发送消息。
     * 通过Redis获取所有设备的映射，并尝试通过TCP通道发送消息。
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
            Channel channel = tcpChannelManager.getChannel(nodeId);
            if (Objects.isNull(channel)) {
                continue;
            }
            channel.writeAndFlush(new Message(userId, message));
        }
    }

    /**
     * 向指定用户和指定设备发送消息。
     * 直接通过Redis获取指定设备的映射，并尝试通过TCP通道发送消息。
     *
     * @param userId 发送消息的用户ID
     * @param device 指定的设备
     * @param message 要发送的消息内容
     */
    @Override
    public void send(String userId, String device, String message) {
        String key = String.format(Constants.clientKeyPrefix, userId, device);
        String nodeId = redisTemplate.opsForValue().get(key);
        log.debug("userId:{} node:{}", userId, nodeId);
        Channel channel = tcpChannelManager.getChannel(nodeId);
        if (Objects.isNull(channel)) {
            return;
        }
        channel.writeAndFlush(new Message(userId, message).setDevice(device));
    }
}
