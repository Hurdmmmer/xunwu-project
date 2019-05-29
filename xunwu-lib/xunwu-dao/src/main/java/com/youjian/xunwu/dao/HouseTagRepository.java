package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseTagRepository extends CrudRepository<HouseTag, Long> {
    List<HouseTag> findAllByHouseId(Long id);

    void deleteByHouseIdAndName(Long houseId, String tag);
}
