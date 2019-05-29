package com.youjian.xunwu.comm.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "house_detail")
public class HouseDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String description;
  @Column(name = "layout_desc")
  private String layoutDesc;
  private String traffic;
  @Column(name = "round_service")
  private String roundService;
  @Column(name = "rent_way")
  private Long rentWay;
  private String address;
  @Column(name = "subway_line_id")
  private Long subwayLineId;
  @Column(name = "subway_line_name")
  private String subwayLineName;
  @Column(name = "subway_station_id")
  private Long subwayStationId;
  @Column(name = "subway_station_name")
  private String subwayStationName;
  @Column(name = "house_id")
  private Long houseId;
}
