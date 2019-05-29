package com.youjian.xunwu.comm.basic;

import lombok.Data;

/**
 * 前端插件 Data table 响应结构
 */
@Data
public class ApiDataTableResponse extends ApiResponse {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;

}
