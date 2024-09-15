package com.nw.im.broker.tcp;

import com.nw.im.common.ChannelManager;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class TcpChannelManager implements ChannelManager {
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    private final AttributeKey<String> clientKey = AttributeKey.newInstance("nodeClient");
    private final Lock lock = new ReentrantLock();
    @Override
    public void addChannel(Channel channel, String client) {
        lock.lock();
        try {
            if (this.online(client)) {
                log.debug("{} 重复连接", client);
                channel.close();
                return;
            }
            channelMap.put(client, channel);
            channel.attr(clientKey).set(client);
            log.debug("{} 连接", client);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeChannel(Channel channel) {
        Object client = this.getClient(channel);
        if (Objects.isNull(client)) {
            return;
        }
        channelMap.remove(client);
        log.debug("{} 移除连接", client);
    }

    @Override
    public String getClient(Channel channel) {
        if (Objects.isNull(channel)) {
            return null;
        }
        return channel.attr(clientKey).get();
    }

    @Override
    public Channel getChannel(String client) {
        return channelMap.get(client);
    }


    @Override
    public Boolean online(String client) {
        return this.channelMap.containsKey(client) && this.channelMap.get(client) != null;
    }
}
