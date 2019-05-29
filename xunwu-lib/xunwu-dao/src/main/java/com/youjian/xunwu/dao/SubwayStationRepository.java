package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubwayStationRepository extends CrudRepository<SubwayStation, Long> {

    List<SubwayStation> findSubwayStationsBySubwayId(Long subwayId);

}
