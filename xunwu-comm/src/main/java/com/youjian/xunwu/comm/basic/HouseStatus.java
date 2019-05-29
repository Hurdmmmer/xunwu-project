package com.youjian.xunwu.comm.basic;

public interface HouseStatus {
    /** 为审核 */
    int UNREVIEWED = 0;
    /** 已审核 */
    int AUDITED = 1;
    /** 已出租 */
    int RENTED = 2;
    /** 逻辑删除 */
    int DELETED =  3;
}
