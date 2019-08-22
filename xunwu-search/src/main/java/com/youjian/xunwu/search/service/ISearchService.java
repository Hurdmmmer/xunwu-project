package com.youjian.xunwu.search.service;

import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.search.HouseBucketDTO;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.comm.vo.ServiceResult;

import java.util.List;

/**
 * 搜索服务类
 */
public interface ISearchService {
    /**
     * create elasticsearch index by houseId
     */
    void createOrUpdateIndex(Long houseId);

    /**
     * remove elasticsearch index by houseId
     */
    void remove(Long houseId);
    /**
     * homepage inquiry
     * */
    ServiceMultiResult<List<Long>> query(RentSearch rentSearch);

    /**
     * keywords suggest
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 根据小区和城市进行聚合统计
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);
    /** 根据城市聚合区域数量 */
    ServiceMultiResult<List<HouseBucketDTO>> mapAggregate(String cityEnName);
}
