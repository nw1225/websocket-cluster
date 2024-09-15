package com.nw.im.connect;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CertificationDetails {
    private Object userId;
    @Builder.Default
    private String device = "default";
}
