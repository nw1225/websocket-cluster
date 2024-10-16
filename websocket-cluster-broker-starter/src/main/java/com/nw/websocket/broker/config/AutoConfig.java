package com.nw.websocket.broker.config;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.nw.websocket.broker.MessageBrokerManage;
import com.nw.websocket.broker.RedisMessageBrokerManage;
import com.nw.websocket.broker.grpc.GrpcChannelManager;
import com.nw.websocket.broker.grpc.NacosNodeSubscriber;
import com.nw.websocket.common.ChannelManager;
import com.nw.websocket.common.grpc.GrpcConnectClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 配置自动装配类
 */
@RequiredArgsConstructor
public class AutoConfig {
    // Websocket属性
    private final WebsocketProperties websocketProperties;
    // Redis模板，用于操作Redis
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 配置Nacos节点订阅者
     *
     * @param nacosServiceManager Nacos服务管理器
     * @param GrpcConnectClient   grpc连接客户端
     * @return Nacos节点订阅者实例
     */
    @ConditionalOnClass(NacosServiceManager.class)
    @Bean
    public EventListener nacosNodeSubscriber(NacosServiceManager nacosServiceManager, GrpcConnectClient GrpcConnectClient) {
        return new NacosNodeSubscriber(nacosServiceManager, GrpcConnectClient, websocketProperties);
    }

    /**
     * 配置grpc通道管理器
     *
     * @return grpc通道管理器实例
     */
    @Bean
    public GrpcChannelManager grpcChannelManager() {
        return new GrpcChannelManager();
    }

    /**
     * 配置grpc 客户端
     *
     * @param channelManager 通道管理器
     * @return grpc 客户端实例
     */
    @Bean
    public GrpcConnectClient grpcConnectClient(ChannelManager channelManager) {
        return new GrpcConnectClient(channelManager);
    }

    /**
     * 配置消息路由管理器
     *
     * @param grpcChannelManager grpc通道管理器
     * @return 消息路由管理器实例
     */
    @Bean
    public MessageBrokerManage messageRouterManage(GrpcChannelManager grpcChannelManager) {
        return new RedisMessageBrokerManage(redisTemplate, grpcChannelManager);
    }
}
