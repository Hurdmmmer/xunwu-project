package com.youjian.xunwu.security.mobile;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ValidateCode implements Serializable {

    public static final String SESSION_SMS_CODE_KEY = "SESSION_KEY_FOR_CODE_SMS";

    /** 随机数 */
    private String code;
    /** 过期事件 */
    protected LocalDateTime expireTime;

    public ValidateCode(String code, int expire) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expire);
    }

    public ValidateCode(String code, LocalDateTime expireTime) {
        this.code = code;
        this.expireTime = expireTime;
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireTime);
    }
}