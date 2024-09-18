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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RedisMessageBrokerManage implements MessageBrokerManage {
    private final RedisTemplate<String, String> redisTemplate;
    private final TcpChannelManager tcpChannelManager;


    @Override
    public void send(String userId, String message) {
        String key = String.format(Constants.clientKeyPrefix, userId, "*");
        ScanOptions scanOptions = ScanOptions.scanOptions().match(key).count(100).build();
        Set<String> collect;
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            collect = cursor.stream().collect(Collectors.toSet());
        }
        List<String> values = redisTemplate.opsForValue().multiGet(collect);
        log.debug("userId:{} node:{}", userId, values);
        for (String nodeId : values) {
            Channel channel = tcpChannelManager.getChannel(nodeId);
            if (Objects.isNull(channel)) {
                continue;
            }
            channel.writeAndFlush(new Message(userId, message));
        }
    }

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
