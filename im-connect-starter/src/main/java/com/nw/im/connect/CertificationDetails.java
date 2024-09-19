package com.nw.im.connect;

import lombok.Builder;
import lombok.Data;

/**
 * 用户认证详情类，用于存储用户身份和设备信息
 */
@Builder
@Data
public class CertificationDetails {
    // 用户身份标识，类型为Object以适应多种数据类型
    private Object userId;
    // 设备信息，默认为"default"，用于标识用户设备
    @Builder.Default
    private String device = "default";
}
