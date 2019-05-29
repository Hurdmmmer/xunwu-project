package com.youjian.xunwu.comm.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private int code;
    private String message;
    private Object data;
    private boolean more;

    public static ApiResponse ofMessage(int code, String message) {
        return new ApiResponse(code, message, null, false);
    }

    public static ApiResponse ofSuccess(Object data) {
        return ofStatus(Status.SUCCESS, data);
    }

    public static ApiResponse ofStatus(Status status, Object data) {
        return new ApiResponse(status.code, status.standardMessage, data, false);
    }

    public static ApiResponse ofStatus(Status status) {
        return new ApiResponse(status.getCode(), status.getStandardMessage(), null, false);
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_VALID_PARAM(40005, "Not Valid Param"),
        NOT_SUPPORTED_OPERATION(40006, "Operation Not Supported"),
        NOT_LOGIN(50000, "Not Login"), NOT_FOUND(40004,"Not Found" );

        private int code;
        private String standardMessage;

    }
}
