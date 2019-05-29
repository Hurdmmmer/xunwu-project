package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;
@Data
@Entity(name = "subway_station")
public class SubwayStation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "subway_id")
  private Long subwayId;
  private String name;
}
