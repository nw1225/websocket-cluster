package com.nw.im.broker.config;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.nw.im.broker.RedisMessageBrokerManage;
import com.nw.im.broker.tcp.NacosNodeSubscriber;
import com.nw.im.broker.tcp.TcpChannelManager;
import com.nw.im.common.ChannelManager;
import com.nw.im.common.tcp.TcpNettyClient;
import com.nw.im.broker.MessageBrokerManage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class AutoConfig {
    private final WebsocketProperties websocketProperties;
    private final RedisTemplate<String, String> redisTemplate;

    @ConditionalOnClass(NacosServiceManager.class)
    @Bean
    public EventListener nacosNodeSubscriber(NacosServiceManager nacosServiceManager, TcpNettyClient tcpNettyClient) {
        return new NacosNodeSubscriber(nacosServiceManager, tcpNettyClient, websocketProperties);
    }


    @Bean
    public TcpChannelManager tcpChannelManager() {
        return new TcpChannelManager();
    }

    @Bean
    public TcpNettyClient tcpNettyClient(ChannelManager channelManager) {
        return new TcpNettyClient(channelManager);
    }

    @Bean
    public MessageBrokerManage messageRouterManage(TcpChannelManager tcpChannelManager) {
        return new RedisMessageBrokerManage(redisTemplate,tcpChannelManager);
    }
}
