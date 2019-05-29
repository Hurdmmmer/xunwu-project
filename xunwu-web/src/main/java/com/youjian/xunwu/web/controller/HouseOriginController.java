package com.youjian.xunwu.web.controller;

import com.youjian.xunwu.comm.basic.ApiResponse;
import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.vo.SubwayStationVo;
import com.youjian.xunwu.comm.vo.SubwayVo;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import com.youjian.xunwu.service.IHouseService;
import com.youjian.xunwu.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class HouseOriginController {

    @Autowired
    IAddressService subWayService;
    @Autowired
    IHouseService houseOriginService;

    @GetMapping("support/cities")
    public ApiResponse supportCities() {
        List<SupportAddressVo> cities = houseOriginService.findCities();
        if (cities != null && !cities.isEmpty()) {
            return ApiResponse.ofSuccess(cities);
        }
        return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND, null);
    }
    @GetMapping("/support/regions")
    public ApiResponse supportRegions(@RequestParam("city_name") String cityName) {
        List<SupportAddressVo> region = houseOriginService.findArea(cityName, SupportAddress.Level.REGION.getValue());
        if (region == null || region.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND, null);
        }
        return ApiResponse.ofSuccess(region);
    }

    @GetMapping("/support/subway/line")
    public ApiResponse supportSubway(@RequestParam("city_name") String cityName) {
        List<SubwayVo> subways = subWayService.findSubwaysByCityName(cityName);
        if (subways == null || subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND, null);
        }
        return ApiResponse.ofSuccess(subways);
    }

    @GetMapping("/support/subway/station")
    public ApiResponse supportSubwayStation(@RequestParam("subway_id") Long subwayId) {
        List<SubwayStationVo> stations = subWayService.findSubwayStationBySubwayId(subwayId);
        if (stations == null || stations.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND, null);
        }
        return ApiResponse.ofSuccess(stations);
    }





}
