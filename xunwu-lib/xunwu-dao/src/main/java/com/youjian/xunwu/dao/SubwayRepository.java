package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.Subway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubwayRepository extends CrudRepository<Subway, Long> {
    List<Subway> findSubwaysByCityEnName(String enName);

}
