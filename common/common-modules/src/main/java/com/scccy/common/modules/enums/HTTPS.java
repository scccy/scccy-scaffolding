package com.scccy.common.modules.enums;

import lombok.Getter;

@Getter
public enum HTTPS {
    CONTENT_TYPE("application/json; charset=utf-8");

    private final String Code;

    HTTPS(String Code) {
        this.Code = Code;
    }
}
