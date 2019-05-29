package com.youjian.xunwu.search.service;

/**
 * 搜索服务类
 */
public interface ISearchService {
    /**
     * create elasticsearch index
     */
    void index(Long houseId);

    /**
     * remove elasticsearch index
     */
    void remove(Long houseId);
}
