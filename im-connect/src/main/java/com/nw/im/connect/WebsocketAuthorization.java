package com.nw.im.connect;

import org.springframework.http.server.ServletServerHttpRequest;

public interface WebsocketAuthorization<T> {
    CertificationDetails<T> verify(ServletServerHttpRequest request);
}
