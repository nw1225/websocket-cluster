package com.nw.im.connect;

import com.nw.im.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
    private final WebsocketProperties websocketProperties;
    private final WebsocketAuthorization<?> websocketAuthorization;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String authorizationTokenName = websocketProperties.getAuthorizationTokenName();
        ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
        String token = serverHttpRequest.getServletRequest().getParameter(authorizationTokenName);
        if (!StringUtils.hasLength(token)) {
            token = serverHttpRequest.getServletRequest().getHeader(authorizationTokenName);
        }
        CertificationDetails<?> certificationDetails = websocketAuthorization.verify(token);
        if (Objects.nonNull(certificationDetails)) {
            attributes.put(Constants.userId, certificationDetails.getUserId());
            attributes.put(Constants.device, certificationDetails.getDevice());
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.debug("connect success");
    }
}
