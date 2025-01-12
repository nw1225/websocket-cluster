package com.nw.websocket.connect.config;

import com.nw.websocket.connect.WebSocketHandler;
import com.nw.websocket.connect.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

// 配置WebSocket的类
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    // 握手拦截器，用于WebSocket的握手过程
    private final HandshakeInterceptor handshakeInterceptor;
    // WebSocket处理程序，处理WebSocket的各类请求
    private final WebSocketHandler webSocketHandler;
    // WebSocket的属性配置，如路径等
    private final WebsocketProperties websocketProperties;

    /**
     * 注册WebSocket处理器
     *
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 在注册表中添加WebSocket处理器
        // 指定WebSocket处理器、路径、允许的源和握手拦截器
        registry.addHandler(webSocketHandler, websocketProperties.getPath())
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor);
    }


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxSessionIdleTimeout(websocketProperties.getMaxSessionIdleTimeout() * 1000L);
        return container;
    }
}
