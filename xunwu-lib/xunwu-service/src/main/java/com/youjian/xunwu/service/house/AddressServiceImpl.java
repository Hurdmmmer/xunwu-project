package com.youjian.xunwu.service.house;

import com.youjian.xunwu.comm.entity.Subway;
import com.youjian.xunwu.comm.entity.SubwayStation;
import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.vo.SubwayStationVo;
import com.youjian.xunwu.comm.vo.SubwayVo;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import com.youjian.xunwu.dao.SubwayRepository;
import com.youjian.xunwu.dao.SubwayStationRepository;
import com.youjian.xunwu.dao.SupportAddressRepository;
import com.youjian.xunwu.service.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private SubwayStationRepository subwayStationRepository;
    @Autowired
    private SupportAddressRepository supportAddressRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<SubwayVo> findSubwaysByCityName(String cityName) {
        List<Subway> subways = subwayRepository.findSubwaysByCityEnName(cityName);
        if (subways == null) {
            return null;
        }
        return subways.stream().map(e -> modelMapper.map(e, SubwayVo.class)).collect(Collectors.toList());
    }

    @Override
    public List<SubwayStationVo> findSubwayStationBySubwayId(Long subwayId) {
        List<SubwayStation> stations = subwayStationRepository.findSubwayStationsBySubwayId(subwayId);
        if (stations == null) {
            return null;
        }
        return stations.stream().map(e -> modelMapper.map(e, SubwayStationVo.class)).collect(Collectors.toList());
    }

    @Override
    public Map<SupportAddress.Level, SupportAddressVo> findSupportAddressByEnNameAndRegionName(String cityEnName, String regionEnName) {
        Map<SupportAddress.Level, SupportAddressVo> result = new HashMap<>();

        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY
                .getValue());
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());
        if (city != null && region != null) {
            result.put(SupportAddress.Level.CITY, modelMapper.map(city, SupportAddressVo.class));
            result.put(SupportAddress.Level.REGION, modelMapper.map(region, SupportAddressVo.class));
        }
        return result;
    }

    @Override
    public List<SupportAddressVo> findAllRegionByCityName(String cityEnName) {

        List<SupportAddress> addresses = supportAddressRepository.findByBelongToAndLevel(cityEnName, SupportAddress.Level.REGION.getValue());

        if (addresses == null || addresses.size() == 0) {
            return null;
        }


        return addresses.stream().map(e -> {
            SupportAddressVo vo = modelMapper.map(e, SupportAddressVo.class);
            vo.setBaiduMapLongitude(e.getBaiduMapLng());
            vo.setBaiduMapLatitude(e.getBaiduMapLat());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SubwayVo findSubway(Long subwayLineId) {
        Optional<Subway> subway = subwayRepository.findById(subwayLineId);
        return subway.map(value -> modelMapper.map(value, SubwayVo.class)).orElse(null);
    }

    @Override
    public SubwayStationVo findSubwayStationBySubwayStationId(Long subwayStationId) {
        return subwayStationRepository.findById(subwayStationId).map(e -> modelMapper.map(e, SubwayStationVo.class)).orElse(null);
    }

    @Override
    public SupportAddressVo findCityByEnName(String cityEnName) {
        SupportAddress byEnName = supportAddressRepository.findByEnName(cityEnName);
        SupportAddressVo map = modelMapper.map(byEnName, SupportAddressVo.class);
        map.setBaiduMapLatitude(byEnName.getBaiduMapLat());
        map.setBaiduMapLongitude(byEnName.getBaiduMapLng());
        return map;
    }

    @Override
    public SupportAddressVo findRegionByEnName(String regionEnName) {
        SupportAddress supportAddress = supportAddressRepository.findByEnName(regionEnName);
        if (supportAddress == null) {
            return null;
        }
        SupportAddressVo vo = modelMapper.map(supportAddress, SupportAddressVo.class);
        vo.setBaiduMapLatitude(supportAddress.getBaiduMapLat());
        vo.setBaiduMapLongitude(supportAddress.getBaiduMapLng());
        return vo;
    }
}
