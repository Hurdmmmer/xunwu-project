package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 地铁线路编号
 */
@Data
@Entity(name = "subway")
public class Subway {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Column(name = "city_en_name")
  private String cityEnName;

}
