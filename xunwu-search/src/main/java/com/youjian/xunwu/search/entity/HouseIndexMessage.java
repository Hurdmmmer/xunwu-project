package com.youjian.xunwu.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseIndexMessage implements Serializable {

    public static final String INDEX = "createOrUpdateIndex";
    public static final String REMOVE = "remove";

    private Long houseId;
    private String operation;
    private int retry = 0;
}
