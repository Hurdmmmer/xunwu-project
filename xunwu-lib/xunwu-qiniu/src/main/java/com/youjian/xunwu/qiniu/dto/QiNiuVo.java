package com.youjian.xunwu.qiniu.dto;

import lombok.Data;

@Data
public final class QiNiuVo {
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;
}
