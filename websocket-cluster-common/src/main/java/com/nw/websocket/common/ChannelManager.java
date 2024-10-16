package com.nw.websocket.common;


import io.grpc.ManagedChannel;

/**
 * 通道管理器接口，用于管理Node端的Channel通道
 */
public interface ChannelManager {

    /**
     * 向管理器中添加一个Channel通道，并与指定的节点ID关联
     *
     * @param channel Channel对象
     * @param nodeId  节点ID，用于标识和查找Channel
     */
    void addChannel(ManagedChannel channel, String nodeId);

    /**
     * 从管理器中移除指定的Channel通道
     *
     * @param channel 要移除的Channel对象
     */
    void removeChannel(ManagedChannel channel);

    /**
     * 获取与指定Channel通道相关联的客户端标识
     *
     * @param channel Channel对象
     * @return 与Channel通道相关联的客户端标识
     */
    String getClient(ManagedChannel channel);

    /**
     * 根据客户端标识获取对应的Channel通道
     *
     * @param client 客户端标识
     * @return 对应的Channel对象，如果没有找到则返回null
     */
    ManagedChannel getChannel(String client);

    /**
     * 检查指定的客户端是否在线
     *
     * @param client 客户端标识
     * @return 如果客户端在线则返回true，否则返回false
     */
    Boolean online(String client);
}
