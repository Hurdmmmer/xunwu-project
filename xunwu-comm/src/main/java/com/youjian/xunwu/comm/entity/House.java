package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "house")
public class House {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title;
  private long price;
  private long area;
  private long room;
  private long floor;
  @Column(name = "total_floor")
  private long totalFloor;
  @Column(name = "watch_times")
  private long watchTimes;
  @Column(name = "build_year")
  private long buildYear;
  private long status;
  @Column(name = "create_time")
  private Date createTime;
  @Column(name = "last_update_time")
  private Date lastUpdateTime;
  @Column(name = "city_en_name")
  private String cityEnName;
  @Column(name = "region_en_name")
  private String regionEnName;
  private String cover;
  private long direction;
  @Column(name = "distance_to_subway")
  private long distanceToSubway;
  private long parlour;
  private String district;
  @Column(name = "admin_id")
  private long adminId;
  private long bathroom;
  private String street;
}
