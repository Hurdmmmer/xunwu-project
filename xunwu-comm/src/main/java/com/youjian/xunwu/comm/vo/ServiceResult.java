package com.youjian.xunwu.comm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceResult<T> {
    private boolean success;
    private T Data;
    private String message;

    public static <T> ServiceResult<T> ofSuccess(T data) {
        return new ServiceResult<>(true, data, null);
    }

    public static <T> ServiceResult<T> ofFail(String message) {
        return new ServiceResult<>(false, null, message);
    }

    public static <T> ServiceResult<T> notFound() {
        return new ServiceResult<>(false, null, "not found");
    }
}
