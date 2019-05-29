package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.SupportAddress;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportAddressRepository extends CrudRepository<SupportAddress, Long> {
    @Query(value = "select id, belong_to, en_name, cn_name, level, baidu_map_lng, baidu_map_lat from support_address where level = :level", nativeQuery = true)
    List<SupportAddress> findAllByLevel(@Param("level") String level);

    List<SupportAddress> findByBelongToAndLevel(String belongTo, String level);

    SupportAddress findByEnNameAndLevel(String enName, String level);

    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);

    SupportAddress findByEnName(String enName);


}
