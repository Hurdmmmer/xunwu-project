package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HousePictureRepository extends CrudRepository<HousePicture, Long> {
    List<HousePicture> findAllByHouseId(Long id);

    HousePicture findByIdAndHouseId(Long coverId, Long targetId);
}
