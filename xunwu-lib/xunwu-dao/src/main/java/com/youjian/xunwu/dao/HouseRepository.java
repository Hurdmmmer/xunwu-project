package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends  PagingAndSortingRepository<House, Long>, JpaSpecificationExecutor<House> {
}
