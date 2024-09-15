package com.nw.example;

import com.nw.im.connect.CertificationDetails;
import com.nw.im.connect.WebsocketAuthorization;
import org.springframework.stereotype.Component;


@Component
public class Authorization implements WebsocketAuthorization {

    @Override
    public CertificationDetails verify(String token) {
        //可以在此处进行权限验证，当用户权限验证通过后，进行握手成功操作，验证失败返回false
        if (token.equals("123")) {
            return null;
        }
        return CertificationDetails.builder().userId(token).build();
    }
}
