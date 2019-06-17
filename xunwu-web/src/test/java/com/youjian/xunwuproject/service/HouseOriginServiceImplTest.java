package com.youjian.xunwuproject.service;

import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.comm.vo.HouseVo;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import com.youjian.xunwu.service.IHouseService;
import com.youjian.xunwu.service.house.HouseServiceImpl;
import com.youjian.xunwuproject.XunwuProjectApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
public class HouseOriginServiceImplTest extends XunwuProjectApplicationTests {
    @Autowired
    IHouseService houseOriginService;

    @Autowired
    private HouseServiceImpl houseService;

    @Test
    public void query() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setKeywords("aaaa");
        rentSearch.setSize(10);
        rentSearch.setStart(0);
        ServiceMultiResult<List<HouseVo>> query = houseService.query(rentSearch);

        System.out.println("query = " + query);
    }

    @Test
    public void findCitiesTest() {
        List<SupportAddressVo> cities = houseOriginService.findCities();
        System.out.println("cities = " + cities);
    }

    @Test
    public void findAreaTest() {
        List<SupportAddressVo> bj = houseOriginService.findArea("bj", "region");
        System.out.println("bj = " + bj);
    }

    @Test
    public void testFindAll() {
        Page<House> by = houseOriginService.findAllBy(null);
        System.out.println("by = " + by);
    }
}
