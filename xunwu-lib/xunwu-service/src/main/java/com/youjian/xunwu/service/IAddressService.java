package com.youjian.xunwu.service;

import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.vo.SubwayStationVo;
import com.youjian.xunwu.comm.vo.SubwayVo;
import com.youjian.xunwu.comm.vo.SupportAddressVo;

import java.util.List;
import java.util.Map;

public interface IAddressService {
    List<SubwayVo> findSubwaysByCityName(String cityName);

    List<SubwayStationVo> findSubwayStationBySubwayId(Long subwayId);

    Map<SupportAddress.Level, SupportAddressVo> findSupportAddressByEnNameAndRegionName(String cityEnName, String regionEnName);

    List<SupportAddressVo> findAllRegionByCityName(String cityEnName);

    SubwayVo findSubway(Long subwayLineId);

    SubwayStationVo findSubwayStationBySubwayStationId(Long subwayStationId);

    SupportAddressVo findCityByEnName(String cityEnName);

    SupportAddressVo findRegionByEnName(String regionEnName);
}
