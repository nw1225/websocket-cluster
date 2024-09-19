package com.nw.im.connect;

import com.nw.im.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Objects;

/**
 * WebSocket握手拦截器，用于在WebSocket连接建立时进行授权验证
 */
@Slf4j
@RequiredArgsConstructor
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
    private final WebsocketProperties websocketProperties;
    private final WebsocketAuthorization websocketAuthorization;

    /**
     * 在握手前进行拦截处理，主要用于授权验证
     *
     * @param request    the request
     * @param response   the response
     * @param wsHandler  the websocket handler
     * @param attributes the attributes map that will be passed to the websocket session
     * @return whether to proceed with the handshake
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        // 获取授权令牌名称
        String authorizationTokenName = websocketProperties.getAuthorizationTokenName();
        // 转换请求为ServletServerHttpRequest类型，以便获取请求参数和头信息
        ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
        // 从请求参数中获取授权令牌
        String token = serverHttpRequest.getServletRequest().getParameter(authorizationTokenName);
        // 如果令牌为空，则从请求头中获取
        if (!StringUtils.hasLength(token)) {
            token = serverHttpRequest.getServletRequest().getHeader(authorizationTokenName);
        }
        // 验证令牌并获取认证详情
        CertificationDetails certificationDetails = websocketAuthorization.verify(token);
        // 如果令牌验证通过，则将用户ID和设备信息存入属性，用于后续处理，并允许握手继续
        if (Objects.nonNull(certificationDetails)) {
            attributes.put(Constants.userId, certificationDetails.getUserId());
            attributes.put(Constants.device, certificationDetails.getDevice());
            return true;
        }
        return false;
    }

    /**
     * 握手后的回调方法，用于处理握手成功后的操作
     *
     * @param request   the request
     * @param response  the response
     * @param wsHandler the websocket handler
     * @param exception the exception that led to the completion of the handshake
     */
    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        // 记录连接成功日志
        log.debug("connect success");
    }
}
