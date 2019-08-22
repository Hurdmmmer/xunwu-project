package com.youjian.xunwu.comm.basic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMultiResult<T>  {
    private boolean success;
    private String msg;
    private int page;
    private int total;
    private T data;

    public static <T> ServiceMultiResult notFound() {
        return ServiceMultiResult.<T>builder().success(false).msg("Not Found").build();
    }

    public static <T> ServiceMultiResult of(T data) {
        return ServiceMultiResult.<T>builder().success(true).data(data).build();
    }
}
