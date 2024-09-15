package com.nw.im.connect;

public interface WebsocketAuthorization<T> {
    CertificationDetails<T> verify(String token);
}
