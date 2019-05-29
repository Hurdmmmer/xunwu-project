package com.youjian.xunwu.service;

import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.form.DataTableSearch;
import com.youjian.xunwu.comm.form.HouseForm;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.comm.vo.HouseVo;
import com.youjian.xunwu.comm.vo.ServiceResult;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IHouseService {
    List<SupportAddressVo> findCities();

    List<SupportAddressVo> findArea(String belongTo, String level);

    ServiceResult save(com.youjian.xunwu.comm.form.HouseForm houseForm);

    Page<House> findAllBy(DataTableSearch dataTableSearch);

    HouseVo findHouseById(Long id);

    HouseVo setHouseCover(Long coverId, Long targetId);

    ServiceResult<HouseVo> updateStatus(Long houseId, Integer code);

    ServiceResult update(HouseForm houseForm);

    ServiceResult deleteTag(Long houseId, String tag);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult deletePic(Long pictureId);
    /** 根据条件查询房源信息
     * @return*/
    ServiceMultiResult<List<HouseVo>> query(RentSearch rentSearch);
}
