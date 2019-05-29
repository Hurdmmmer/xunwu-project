package com.youjian.xunwuproject.service;

import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import com.youjian.xunwu.service.house.AddressServiceImpl;
import com.youjian.xunwuproject.XunwuProjectApplicationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class AddressServiceTest extends XunwuProjectApplicationTests {
    @Autowired
    private AddressServiceImpl addressService;

    @Test
    public void testFindSupportAddress() {
        Map<SupportAddress.Level, SupportAddressVo> supportAddressByEnNameAndRegionName = addressService.findSupportAddressByEnNameAndRegionName("bj", "dcq");

        System.out.println("supportAddressByEnNameAndRegionName = " + supportAddressByEnNameAndRegionName);
    }
}
