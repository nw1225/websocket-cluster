package com.nw.websocket.connect;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置WebSocket相关属性的类
 * 通过使用@ConfigurationProperties注解，将配置文件中前缀为"websocket"的属性绑定到此对象
 */
@Data
@ConfigurationProperties(prefix = "websocket")
public class WebsocketProperties {
    /**
     * 指定授权令牌的参数名称，默认为"token"
     */
    private String authorizationTokenName = "token";

    /**
     * 定义WebSocket的路径，默认为"/ws"
     */
    private String path = "/ws";

    /**
     * 最大空闲时间，默认120s
     */
    private Long maxSessionIdleTimeout = 120L;
}
