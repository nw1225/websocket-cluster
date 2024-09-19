package com.nw.im.broker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置WebSocket相关属性的类
 * 该类用于通过Spring Boot的配置属性来初始化和管理WebSocket集群的配置
 */
@Data
@ConfigurationProperties(prefix = "websocket.cluster")
public class WebsocketProperties {
    /**
     * WebSocket集群的连接服务名称
     * 这个属性用来标识WebSocket服务在集群中的名称，用于服务发现和管理
     */
    private String connectServiceName;

}
