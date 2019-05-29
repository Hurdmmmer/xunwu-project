package com.youjian.xunwu.comm.entity;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;

@Entity(name = "support_address")
@Data
public class SupportAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "belong_to")
    private String belongTo;
    @Column(name = "en_name")
    private String enName;
    @Column(name = "cn_name")
    private String cnName;
    private String level;
    @Column(name = "baidu_map_lng")
    private double baiduMapLng;
    @Column(name = "baidu_map_lat")
    private double baiduMapLat;

    @Getter
    public static enum Level {
        CITY ("city") {
            @Override
            public String toString() {
                return "city";
            }
        },
        REGION("region") {
            @Override
            public String toString() {
                return "region";
            }
        };

        private String value;

        Level(String value) {
            this.value = value;
        }
    }
}
