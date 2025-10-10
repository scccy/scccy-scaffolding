package com.scccy.common.modules.enums;

import lombok.Getter;

@Getter
public enum ThirdPath {

    FEISHU_APP_ID("cli_a8526b9c4cb8900e"),
    FEISHU_APP_SECRET("mTDYILEgEYDA70xCFQiFkgDiNJ1BQrny"),

    FEISHU_LARK_BASE_URL("https://open.feishu.cn/open-apis"),
    FEISHU_LARK_PASSPORT_HOST("https://passport.feishu.cn/suite/passport/oauth/");


    private final String Code;

    ThirdPath(String Code) {
        this.Code = Code;
    }
}
