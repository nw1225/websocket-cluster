package com.nw.im.connect;

public interface WebsocketAuthorization {
    CertificationDetails verify(String token);
}
