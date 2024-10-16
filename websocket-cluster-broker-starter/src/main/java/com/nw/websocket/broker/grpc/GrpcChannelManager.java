package com.nw.websocket.broker.grpc;

import com.nw.websocket.common.ChannelManager;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于grpc协议的通道管理器实现类
 * 负责管理客户端与服务器之间的连接通道
 */
@Slf4j
public class GrpcChannelManager implements ChannelManager {
    // 存储客户端连接通道的映射
    private final Map<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();
    // 用于标识客户端的属性键
    private final Map<ManagedChannel, String> channelKeyMap = new ConcurrentHashMap<>();

    // 锁，用于同步访问channel
    private final Lock lock = new ReentrantLock();

    /**
     * 添加一个客户端连接通道
     * 如果客户端已经在线，则关闭当前连接；否则，将连接添加到映射中
     *
     * @param channel 客户端连接通道
     * @param client  客户端标识
     */
    @Override
    public void addChannel(ManagedChannel channel, String client) {
        lock.lock();
        try {
            if (this.online(client)) {
                log.debug("{} 重复连接", client);
                channel.shutdown();
                return;
            }
            channelMap.put(client, channel);
            channelKeyMap.put(channel, client);

            log.debug("{} 连接", client);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 移除一个客户端连接通道
     * 根据通道获取客户端标识，并从映射中移除对应的连接
     *
     * @param channel 客户端连接通道
     */
    @Override
    public void removeChannel(ManagedChannel channel) {
        lock.lock();
        try {
            Object client = this.getClient(channel);
            if (Objects.isNull(client)) {
                return;
            }
            channelMap.remove(client);
            channelKeyMap.remove(channel);

            log.debug("{} 移除连接", client);

        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取通道所属的客户端标识
     * 如果通道为空，则返回null
     *
     * @param channel 客户端连接通道
     * @return 客户端标识或null
     */
    @Override
    public String getClient(ManagedChannel channel) {
        if (Objects.isNull(channel)) {
            return null;
        }
        return channelKeyMap.get(channel);
    }

    /**
     * 根据客户端标识获取连接通道
     *
     * @param client 客户端标识
     * @return 连接通道或null
     */
    @Override
    public ManagedChannel getChannel(String client) {
        return channelMap.get(client);
    }

    /**
     * 检查客户端是否在线
     * 通过检查映射中是否存在对应的客户端标识以及其连接通道是否非空来判断
     *
     * @param client 客户端标识
     * @return 如果客户端在线返回true，否则返回false
     */
    @Override
    public Boolean online(String client) {
        return this.channelMap.containsKey(client);
    }
}
