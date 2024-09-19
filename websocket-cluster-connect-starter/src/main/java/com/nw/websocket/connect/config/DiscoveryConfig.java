package com.nw.websocket.connect.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.nw.websocket.common.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

/**
 * 配置类，用于处理服务发现和注册的相关配置
 */
@RequiredArgsConstructor
public class DiscoveryConfig {

    /**
     * 条件性创建NacosWatch实例
     *
     * @param nacosServiceManager      管理Nacos服务的管理器
     * @param nacosDiscoveryProperties Nacos发现属性配置
     * @param tcpProperties            TCP属性配置
     * @return 返回配置好的NacosWatch实例
     */
    @ConditionalOnClass(NacosServiceManager.class)
    @Bean
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties, TcpProperties tcpProperties) {
        // 获取元数据信息，用于服务注册时附加信息
        var metadata = nacosDiscoveryProperties.getMetadata();
        // 如果元数据为空，则初始化
        if (metadata == null) {
            metadata = new HashMap<>();
            nacosDiscoveryProperties.setMetadata(metadata);
        }
        // 在元数据中添加节点端口和节点ID信息，用于服务区分与路由
        metadata.put(Constants.NODE_PORT, tcpProperties.getPort().toString());
        metadata.put(Constants.NODE_ID, Constants.nodeId);
        // 创建并返回NacosWatch实例，用于监控服务变更
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }
}
