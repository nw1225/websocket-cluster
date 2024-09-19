package com.nw.websocket.common;

import io.netty.channel.Channel;

/**
 * 通道管理器接口，用于管理Node端的Channel通道
 */
public interface ChannelManager {

    /**
     * 向管理器中添加一个Channel通道，并与指定的节点ID关联
     *
     * @param channel Netty的Channel对象
     * @param nodeId  节点ID，用于标识和查找Channel
     */
    void addChannel(Channel channel, String nodeId);

    /**
     * 从管理器中移除指定的Channel通道
     *
     * @param channel 要移除的Netty Channel对象
     */
    void removeChannel(Channel channel);

    /**
     * 获取与指定Channel通道相关联的客户端标识
     *
     * @param channel Netty的Channel对象
     * @return 与Channel通道相关联的客户端标识
     */
    String getClient(Channel channel);

    /**
     * 根据客户端标识获取对应的Channel通道
     *
     * @param client 客户端标识
     * @return 对应的Netty Channel对象，如果没有找到则返回null
     */
    Channel getChannel(String client);

    /**
     * 检查指定的客户端是否在线
     *
     * @param client 客户端标识
     * @return 如果客户端在线则返回true，否则返回false
     */
    Boolean online(String client);
}
