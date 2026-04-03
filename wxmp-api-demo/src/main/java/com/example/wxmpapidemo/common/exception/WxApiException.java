package com.example.wxmpapidemo.common.exception;

import lombok.Getter;

@Getter
public class WxApiException extends RuntimeException {

    private final int code;

    public WxApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WxApiException(String message) {
        this(400, message);
    }
}
