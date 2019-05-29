package com.youjian.xunwu.comm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "house_tag")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HouseTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "house_id")
  private Long houseId;
  private String name;

}
