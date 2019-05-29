package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "house_subscribe")
public class HouseSubscribe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "house_id")
    private Long houseId;
    @Column(name = "user_id")
    private Long userId;
    private String desc;
    private Long status;
    @Column(name = "create_time")
    private java.sql.Timestamp createTime;
    @Column(name = "last_update_time")
    private java.sql.Timestamp lastUpdateTime;
    @Column(name = "order_time")
    private java.sql.Timestamp orderTime;
    private String telephone;
    @Column(name = "admin_id")
    private Long adminId;

}
