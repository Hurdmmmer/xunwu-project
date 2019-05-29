package com.youjian.xunwu.comm.form;

import com.google.common.collect.ImmutableMap;
import lombok.Data;

import java.util.Map;

/**
 * 带区间的常用数值定义
 */
@Data
public class RentValueBlock {
    /**
     * 价格区间定义
     */
    public static final Map<String, RentValueBlock> PRICE_BLOCK;
    /**
     * 面积区间定义
     */
    public static final Map<String, RentValueBlock> AREA_BLOCK;

    public static final RentValueBlock ALL = new RentValueBlock("*", -1, -1);

    // 初始化价格区间
    static {
        PRICE_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-1000", new RentValueBlock("*-1000", -1, 1000))
                .put("1000-3000", new RentValueBlock("1000-3000", 1000, 3000))
                .put("3000-*", new RentValueBlock("3000-*", 3000, -1))
                .build();

        AREA_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-30", new RentValueBlock("*-30", -1, 30))
                .put("30-60", new RentValueBlock("30-60", 30, 60))
                .put("60-*", new RentValueBlock("60-*", 60, -1))
                .build();
    }

    private String key;
    private int min;
    private int max;

    private RentValueBlock(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    /**
     * 价格区间匹配
     */
    public static RentValueBlock matchPrice(String key) {
        RentValueBlock rentValueBlock = PRICE_BLOCK.get(key);
        if (rentValueBlock == null) {
            return ALL;
        }
        return rentValueBlock;
    }

    public static RentValueBlock matchArea(String key) {
        RentValueBlock rentValueBlock = AREA_BLOCK.get(key);
        if (rentValueBlock == null) {
            return ALL;
        }
        return rentValueBlock;
    }
}
