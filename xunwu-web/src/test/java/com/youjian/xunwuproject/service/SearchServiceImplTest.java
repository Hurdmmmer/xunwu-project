package com.youjian.xunwuproject.service;


import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.dao.HouseRepository;
import com.youjian.xunwu.search.service.ISearchService;
import com.youjian.xunwuproject.XunwuProjectApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SearchServiceImplTest extends XunwuProjectApplicationTests {
    @Autowired
    private ISearchService searchService;
    @Autowired
    private HouseRepository houseRepository;

    @Test
    public void query() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setSize(10);
        rentSearch.setStart(0);
        ServiceMultiResult<List<Long>> query = searchService.query(rentSearch);
        System.out.println("query = " + query);
    }

    @Test
    public void testDeleteAndCreateIndex() {
        Iterable<House> all = houseRepository.findAll();
        for (House house : all) {
            try {
                searchService.remove(house.getId());
            } catch (Exception ignored) {

            }
            searchService.createOrUpdateIndex(house.getId());
        }

    }
}
