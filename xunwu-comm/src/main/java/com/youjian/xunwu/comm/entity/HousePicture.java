package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "house_picture")
public class HousePicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "house_id")
    private Long houseId;
    @Column(name = "cdn_prefix")
    private String cdnPrefix;
    private Long width;
    private Long height;
    private String location;
    private String path;

}
