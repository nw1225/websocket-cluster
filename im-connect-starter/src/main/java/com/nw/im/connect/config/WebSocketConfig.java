package com.nw.im.connect.config;

import com.nw.im.connect.WebSocketHandler;
import com.nw.im.connect.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final HandshakeInterceptor handshakeInterceptor;
    private final WebSocketHandler webSocketHandler;
    private final WebsocketProperties websocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, websocketProperties.getPath())
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor);
    }


}
