package com.nw.im.connect.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.nw.im.common.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@RequiredArgsConstructor
public class DiscoveryConfig {

    @ConditionalOnClass(NacosServiceManager.class)
    @Bean
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties, TcpProperties tcpProperties) {
        var metadata = nacosDiscoveryProperties.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            nacosDiscoveryProperties.setMetadata(metadata);
        }
        metadata.put(Constants.NODE_PORT, tcpProperties.getPort().toString());
        metadata.put(Constants.NODE_ID, Constants.nodeId);
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }
}
