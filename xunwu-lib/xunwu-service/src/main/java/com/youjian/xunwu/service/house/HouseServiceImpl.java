package com.youjian.xunwu.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.youjian.xunwu.comm.basic.HouseDirection;
import com.youjian.xunwu.comm.basic.HouseStatus;
import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.*;
import com.youjian.xunwu.comm.form.*;
import com.youjian.xunwu.comm.utils.SecurityKit;
import com.youjian.xunwu.comm.vo.*;
import com.youjian.xunwu.dao.*;
import com.youjian.xunwu.qiniu.config.QiniuConfig;
import com.youjian.xunwu.qiniu.service.QiniuService;
import com.youjian.xunwu.search.service.ISearchService;
import com.youjian.xunwu.service.IHouseService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class HouseServiceImpl implements IHouseService {
    @Autowired
    SupportAddressRepository supportAddressRepository;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private HousePictureRepository housePictureRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private QiniuConfig qiniuConfig;
    @Autowired
    private SubwayStationRepository subwayStationRepository;
    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private QiniuService qiniuService;
    @Autowired
    private ISearchService searchService;


    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<SupportAddressVo> findCities() {
        List<SupportAddress> addresses = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        if (addresses == null) {
            return null;
        }
        return addresses.stream().map(e -> modelMapper.map(e, SupportAddressVo.class)).collect(Collectors.toList());
    }

    @Override
    public List<SupportAddressVo> findArea(String belongTo, String level) {
        List<SupportAddress> addresses = supportAddressRepository.findByBelongToAndLevel(belongTo, level);
        if (addresses == null) {
            return null;
        }
        return addresses.stream().map(e -> modelMapper.map(e, SupportAddressVo.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServiceResult save(com.youjian.xunwu.comm.form.HouseForm houseForm) {

        Long currentUserId = SecurityKit.getCurrentUserId();

        House house = modelMapper.map(houseForm, House.class);


        house.setCreateTime(new Date());
        house.setLastUpdateTime(new Date());
        house.setAdminId(currentUserId);
        house = houseRepository.save(house);
        ServiceResult result = wrapperHouseDetailInfo(houseForm);
        if (!result.isSuccess()) {
            log.warn("设置房源详情错误: {}", result.getMessage());
            return result;
        }
        HouseDetail houseDetail = houseDetailRepository.save((HouseDetail) result.getData());

        HouseVo houseVo = modelMapper.map(house, HouseVo.class);

        houseVo.setHouseDetail(modelMapper.map(houseDetail, HouseDetailVo.class));
        houseVo.setCover(this.qiniuConfig.getCdnPrefix() + houseVo.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null && tags.size() > 0) {
            House finalHouse = house;
            List<HouseTag> houseTags = tags.stream().
                    map(e -> HouseTag.builder().houseId(finalHouse.getId()).name(e).build()).collect(Collectors.toList());
            houseTagRepository.saveAll(houseTags);
            houseVo.setTags(tags);
        }

        List<PhotoForm> photos = houseForm.getPhotos();
        if (photos != null && photos.size() > 0) {
            House finalHouse1 = house;
            List<HousePicture> housePictures = photos.stream().map(e -> {
                HousePicture map = modelMapper.map(e, HousePicture.class);
                map.setHouseId(finalHouse1.getId());
                map.setCdnPrefix(this.qiniuConfig.getCdnPrefix());
                return map;
            }).collect(Collectors.toList());
            Iterable<HousePicture> pictures = this.housePictureRepository.saveAll(housePictures);
            houseVo.setPictures(Stream.of(pictures).map(e -> modelMapper.map(e, HousePictureVo.class)).collect(Collectors.toList()));
        }

        return ServiceResult.ofSuccess(houseVo);
    }

    /**
     * 包装 houseDetail 对象
     */
    private ServiceResult wrapperHouseDetailInfo(HouseForm houseForm) {
        HouseDetail houseDetail = modelMapper.map(houseForm, HouseDetail.class);
        if (houseForm.getId() != null) {
            HouseDetail detail = houseDetailRepository.findByHouseId(houseForm.getId());
            if (detail != null) {
                houseDetail.setId(detail.getId());
            }
        }
        houseDetail.setAddress(houseForm.getDetailAddress());
        houseDetail.setHouseId(houseForm.getId());
        Optional<Subway> optionalSubway = this.subwayRepository.findById(houseForm.getSubwayLineId());
        optionalSubway.ifPresent(subway -> houseDetail.setSubwayLineName(subway.getName()));
        if (!optionalSubway.isPresent()) {
            return ServiceResult.ofFail("Not Valid Subway Line");
        }
        Optional<SubwayStation> optionalSubwayStation = this.subwayStationRepository.findById(houseForm.getSubwayStationId());
        optionalSubwayStation.ifPresent(e -> houseDetail.setSubwayStationName(e.getName()));
        if (!optionalSubwayStation.isPresent() || !optionalSubway.get().getId().equals(optionalSubwayStation.get().getSubwayId())) {
            return ServiceResult.ofFail("Not valid Subway Station");
        }
        return ServiceResult.ofSuccess(houseDetail);
    }


    @Override
    public Page<House> findAllBy(DataTableSearch dataTableSearch) {

        int length = dataTableSearch.getLength();
        int start = dataTableSearch.getStart() / dataTableSearch.getLength();

        Integer status = dataTableSearch.getStatus();
        String city = dataTableSearch.getCity();
        String title = dataTableSearch.getTitle();

        // root : 表示当前查询的对象, 如: 当前 root 代表 House 对象
        // criteriaBuilder 构建条件对象

        Specification<House> specification = (root, criteriaQuery, criteriaBuilder) -> {

            Predicate predicate = null;
            if (!SecurityKit.SUPER_ADMIN.equals(SecurityKit.getCurrentUserId())) {
                predicate = criteriaBuilder.equal(root.get("adminId"), SecurityKit.getCurrentUserId());
            }

            predicate = criteriaBuilder.and(predicate, criteriaBuilder.notEqual(root.get("status"), HouseStatus.DELETED));

            if (city != null && city.trim().length() > 0) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), city));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            if (dataTableSearch.getCreateTimeMin() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), dataTableSearch.getCreateTimeMin()));
            }
            if (dataTableSearch.getCreateTimeMax() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), dataTableSearch.getCreateTimeMin()));
            }

            if (title != null && title.trim().length() > 0) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("title"), "%" + title + "%"));

            }

            return predicate;
        };


        Sort sort = Sort.by(Sort.Direction.fromString(dataTableSearch.getDirection()), dataTableSearch.getOrderBy());
        PageRequest page = PageRequest.of(start, length, sort);
        return houseRepository.findAll(specification, page);
    }

    @Override
    public HouseVo findHouseById(Long id) {

        Optional<House> dbHouse = houseRepository.findById(id);
        if (!dbHouse.isPresent()) {
            return null;
        }
        House house = dbHouse.get();
        HouseVo result = modelMapper.map(house, HouseVo.class);
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(house.getId());
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseId(house.getId());
        List<HousePicture> housePictures = housePictureRepository.findAllByHouseId(house.getId());
        if (houseDetail != null) {
            result.setHouseDetail(modelMapper.map(houseDetail, HouseDetailVo.class));
        }
        if (houseTags != null && houseTags.size() > 0) {
            result.setTags(houseTags.stream().map(HouseTag::getName).collect(Collectors.toList()));
        }
        if (housePictures != null && housePictures.size() > 0) {
            result.setPictures(housePictures.stream().map(e -> modelMapper.map(e, HousePictureVo.class)).collect(Collectors.toList()));
        }
        result.setCover(this.qiniuConfig.getCdnPrefix() + result.getCover());

        return result;
    }

    @Override
    @Transactional
    public HouseVo setHouseCover(Long coverId, Long targetId) {
        Optional<House> house = houseRepository.findById(targetId);
        if (!house.isPresent()) {
            return null;
        }
        HousePicture housePicture = housePictureRepository.findByIdAndHouseId(coverId, targetId);
        if (housePicture == null) {
            return null;
        }
        House dbHouse = house.get();
        dbHouse.setCover(housePicture.getPath());
        houseRepository.save(dbHouse);
        return modelMapper.map(dbHouse, HouseVo.class);
    }

    @Override
    @Transactional
    public ServiceResult<HouseVo> updateStatus(Long houseId, Integer status) {
        Optional<House> optionalHouse = houseRepository.findById(houseId);
        if (!optionalHouse.isPresent()) {
            return ServiceResult.ofFail("该房源不存在");
        }

        House house = optionalHouse.get();
        if (status == house.getStatus()) {
            return ServiceResult.ofFail("房屋状态没有改变");
        }
        if (house.getStatus() == HouseStatus.RENTED) {
            return ServiceResult.ofFail("已出租的房源不能修改状态");
        }
        if (house.getStatus() == HouseStatus.DELETED) {
            return ServiceResult.ofFail("已删除的房源不能修改状态");
        }

        house.setStatus(status);
        houseRepository.save(house);
        if (house.getStatus() == HouseStatus.AUDITED) {
            searchService.index(houseId);
        } else {
            searchService.remove(houseId );
        }
        return ServiceResult.ofSuccess(modelMapper.map(house, HouseVo.class));
    }

    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        Optional<House> optHouse = houseRepository.findById(houseForm.getId());
        if (!optHouse.isPresent()) {
            return ServiceResult.notFound();
        }
        ServiceResult serviceResult = wrapperHouseDetailInfo(houseForm);
        if (serviceResult.isSuccess()) {
            Object data = serviceResult.getData();
            houseDetailRepository.save((HouseDetail) data);
        } else {
            return ServiceResult.notFound();
        }

        House house = modelMapper.map(houseForm, House.class);
        house.setAdminId(SecurityKit.getCurrentUserId());
        house.setCreateTime(optHouse.map(House::getCreateTime).orElse(null));
        house.setLastUpdateTime(new Date());
        house.setCover(optHouse.get().getCover());
        houseRepository.save(house);

        List<PhotoForm> photos = houseForm.getPhotos();
        if (photos != null && photos.size() > 0) {
            List<HousePicture> housePictures = photos.stream().map(e ->
                    modelMapper.map(e, HousePicture.class)).peek(e -> {
                e.setHouseId(house.getId());
                e.setCdnPrefix(this.qiniuConfig.getCdnPrefix());
            }).collect(Collectors.toList());
            housePictureRepository.saveAll(housePictures);
        }

        if (house.getStatus() == HouseStatus.AUDITED) {
            searchService.index(house.getId());
        }

        return ServiceResult.ofSuccess(null);
    }


    @Override
    @Transactional
    public ServiceResult deleteTag(Long houseId, String tag) {

        Optional<House> optHouse = houseRepository.findById(houseId);
        if (!optHouse.isPresent()) {
            return ServiceResult.notFound();
        }
        houseTagRepository.deleteByHouseIdAndName(houseId, tag);
        return ServiceResult.ofSuccess(null);
    }

    @Override
    @Transactional
    public ServiceResult addTag(Long houseId, String tag) {
        Optional<House> optHouse = houseRepository.findById(houseId);
        if (!optHouse.isPresent()) {
            return ServiceResult.notFound();
        }
        houseTagRepository.save(HouseTag.builder().houseId(houseId).name(tag).build());
        return ServiceResult.ofSuccess(null);
    }

    @Override
    @Transactional
    public ServiceResult deletePic(Long pictureId) {
        Optional<HousePicture> optPicture = housePictureRepository.findById(pictureId);
        if (!optPicture.isPresent()) {
            return ServiceResult.notFound();
        }
        HousePicture housePicture = optPicture.get();

        try {
            // 如果删除七牛云成功在删除数据库中的数据
            Response response = qiniuService.deleteFile(housePicture.getPath());
            if (response.isOK()) {
                Long houseId = housePicture.getHouseId();
                Optional<House> optHouse = houseRepository.findById(houseId);
                if (optHouse.isPresent() && optHouse.get().getCover().equalsIgnoreCase(housePicture.getPath())) {
                    House house = optHouse.get();
                    house.setCover(null);
                    houseRepository.save(house);
                }

                housePictureRepository.delete(housePicture);
            } else {
                log.warn("七牛云删除失败: {}", response.error);
                return ServiceResult.ofFail(response.error);
            }
        } catch (QiniuException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }


        return ServiceResult.ofSuccess(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServiceMultiResult<List<HouseVo>> query(RentSearch rentSearch) {
        Sort sort = Sort.by(Sort.Direction.fromString(rentSearch.getOrderDirection()), rentSearch.getOrderBy());
        int page = rentSearch.getStart() / rentSearch.getSize();
        PageRequest pageRequest = PageRequest.of(page, rentSearch.getSize(), sort);

        Specification<House> specification = (root, cq, cb) -> {
            Predicate predicate = null;
            String regionEnName = rentSearch.getRegionEnName();
            predicate = cb.notEqual(root.get("status"), HouseStatus.DELETED);
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.RENTED));
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.UNREVIEWED));
            if (regionEnName != null && !regionEnName.equalsIgnoreCase("*")) {
                predicate = cb.and(predicate, cb.equal(root.get("regionEnName"), regionEnName));
            }
            if (rentSearch.getDirection() > HouseDirection.ALL && rentSearch.getDirection() <= HouseDirection.NORTH_AND_SOUTH) {
                predicate = cb.and(predicate, cb.equal(root.get("direction"), rentSearch.getDirection()));
            }
            if (rentSearch.getAreaBlock() != null) {
                RentValueBlock rentValueBlock = RentValueBlock.matchArea(rentSearch.getAreaBlock());
                predicate = generatorPredicate(predicate, root, cb, rentValueBlock, "area");
            }
            if (rentSearch.getPriceBlock() != null) {
                RentValueBlock rentValueBlock = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
                predicate = generatorPredicate(predicate, root, cb, rentValueBlock, "price");
            }
            if (rentSearch.getKeywords() != null && rentSearch.getKeywords().trim().length() > 0) {
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + rentSearch.getKeywords() + "%"));
            }
            if (rentSearch.getRoom() > 0) {
                predicate = cb.and(predicate, cb.equal(root.get("room"), rentSearch.getRoom()));
            }


            return predicate;
        };

        Page<House> houses = this.houseRepository.findAll(specification, pageRequest);

        List<House> content = houses.getContent();
        List<HouseVo> vos = new ArrayList<>();
        for (House house : content) {
            HouseVo vo = modelMapper.map(house, HouseVo.class);
            HouseDetail houseDetail = this.houseDetailRepository.findByHouseId(house.getId());
            if (houseDetail != null) {
                vo.setHouseDetail(modelMapper.map(houseDetail, HouseDetailVo.class));
            }
            vo.setCover(qiniuConfig.getCdnPrefix() + vo.getCover());
            List<HouseTag> tags = houseTagRepository.findAllByHouseId(house.getId());
            if (tags != null) {
                vo.setTags(tags.stream().map(HouseTag::getName).collect(Collectors.toList()));
            }
            vos.add(vo);
        }
        if (vos.size() == 0) {
            return ServiceMultiResult.<List<HouseVo>>notFound();
        }

        return ServiceMultiResult.<List<HouseVo>>builder()
                .page(page)
                .total((int) houses.getTotalElements())
                .success(true)
                .data(vos)
                .build();
    }

    private Predicate generatorPredicate(Predicate predicate, Root<House> root, CriteriaBuilder cb, RentValueBlock rentValueBlock, String field) {
        int min = rentValueBlock.getMin();
        int max = rentValueBlock.getMax();
        if (min < 0 && max > 0) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(field), max));
        } else if (min > 0 && max < 0) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(field), min));
        } else if (min > 0 && max > 0 && max > min) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(field), min));
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(field), max));
        }
        return predicate;
    }
}
