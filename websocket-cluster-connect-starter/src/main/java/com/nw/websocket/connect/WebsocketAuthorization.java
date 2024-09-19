package com.nw.websocket.connect;

/**
 * Websocket授权接口，用于验证用户的访问令牌
 */
public interface WebsocketAuthorization {
    /**
     * 验证给定的令牌并返回认证详情
     *
     * @param token 待验证的用户令牌
     * @return 包含认证信息的CertificationDetails对象如果验证失败，返回null
     */
    CertificationDetails verify(String token);
}
