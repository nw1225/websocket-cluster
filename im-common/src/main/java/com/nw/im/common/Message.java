package com.nw.im.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {
    private final String userId;
    private final String message;
}

