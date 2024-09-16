package com.nw.im.broker;

import com.nw.im.common.Constants;
import com.nw.im.common.tcp.Message;
import com.nw.im.broker.tcp.TcpChannelManager;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RedisMessageBrokerManage implements MessageBrokerManage {
    private final RedisTemplate<String, ?> redisTemplate;
    private final TcpChannelManager tcpChannelManager;


    @Override
    public void send(String userId, String message) {
        List<Object> values = redisTemplate.opsForHash().values(Constants.clientKeyPrefix + userId);
        Set<String> collect = values.stream().map(Object::toString).collect(Collectors.toSet());
        log.debug("userId:{} node:{}", userId, values);
        for (String nodeId : collect) {
            Channel channel = tcpChannelManager.getChannel(nodeId);
            if (Objects.isNull(channel)) {
                continue;
            }
            channel.writeAndFlush(Message.builder().message(message).userId(userId).build());
        }
    }
}
