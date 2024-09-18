package com.nw.im.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Data
public class Message implements Serializable {
    private final String userId;
    private String device;
    private final String message;
}

