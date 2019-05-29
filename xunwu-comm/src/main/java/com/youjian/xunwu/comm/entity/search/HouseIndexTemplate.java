package com.youjian.xunwu.comm.entity.search;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *  ES 中索引映射
 */
@Data
public class HouseIndexTemplate {
    private Long houseId;
    private String title;
    private Integer price;
    private Integer area;
    private Date createTime;
    private Date lastUpdateTime;
    private String cityEnName;
    private String regionEnName;
    private Integer direction;
    private Integer distanceToSubway;
    private String subwayStationName;
    private String subwayLineName;
    private List<String> tags;  // es 中 text 类型 可以使用 array list 来映射
    private String street;
    private String district;
    private String description;
    private String layoutDesc;
    private String traffic;
    private String roundService;
    /** 出租方式 */
    private Integer rentWay;

}
