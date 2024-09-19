package com.nw.im.broker.config;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.nw.im.broker.MessageBrokerManage;
import com.nw.im.broker.RedisMessageBrokerManage;
import com.nw.im.broker.tcp.NacosNodeSubscriber;
import com.nw.im.broker.tcp.TcpChannelManager;
import com.nw.im.common.ChannelManager;
import com.nw.im.common.tcp.TcpNettyClient;
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
     * @param tcpNettyClient      TCP Netty客户端
     * @return Nacos节点订阅者实例
     */
    @ConditionalOnClass(NacosServiceManager.class)
    @Bean
    public EventListener nacosNodeSubscriber(NacosServiceManager nacosServiceManager, TcpNettyClient tcpNettyClient) {
        return new NacosNodeSubscriber(nacosServiceManager, tcpNettyClient, websocketProperties);
    }

    /**
     * 配置TCP通道管理器
     *
     * @return TCP通道管理器实例
     */
    @Bean
    public TcpChannelManager tcpChannelManager() {
        return new TcpChannelManager();
    }

    /**
     * 配置TCP Netty客户端
     *
     * @param channelManager 通道管理器
     * @return TCP Netty客户端实例
     */
    @Bean
    public TcpNettyClient tcpNettyClient(ChannelManager channelManager) {
        return new TcpNettyClient(channelManager);
    }

    /**
     * 配置消息路由管理器
     *
     * @param tcpChannelManager TCP通道管理器
     * @return 消息路由管理器实例
     */
    @Bean
    public MessageBrokerManage messageRouterManage(TcpChannelManager tcpChannelManager) {
        return new RedisMessageBrokerManage(redisTemplate, tcpChannelManager);
    }
}
