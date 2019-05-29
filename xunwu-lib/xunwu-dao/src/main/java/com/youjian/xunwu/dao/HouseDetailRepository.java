package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.HouseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseDetailRepository extends JpaRepository<HouseDetail, Long> {
    HouseDetail findByHouseId(Long id);
}
