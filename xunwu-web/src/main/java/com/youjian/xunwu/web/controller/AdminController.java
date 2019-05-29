package com.youjian.xunwu.web.controller;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.youjian.xunwu.comm.basic.ApiDataTableResponse;
import com.youjian.xunwu.comm.basic.ApiResponse;
import com.youjian.xunwu.comm.basic.HouseStatus;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.form.DataTableSearch;
import com.youjian.xunwu.comm.form.HouseForm;
import com.youjian.xunwu.comm.utils.SecurityKit;
import com.youjian.xunwu.comm.vo.*;
import com.youjian.xunwu.qiniu.config.QiniuConfig;
import com.youjian.xunwu.qiniu.dto.QiNiuVo;
import com.youjian.xunwu.qiniu.service.IQiniuService;
import com.youjian.xunwu.service.IAddressService;
import com.youjian.xunwu.service.IHouseService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.youjian.xunwu.comm.basic.HouseOperate.*;

/**
 * 管理员控制器
 */
@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    @Autowired
    IQiniuService qiniuService;

    @Autowired
    Gson gson;

    @Autowired
    private IAddressService addressService;
    @Autowired
    private QiniuConfig qiniuConfig;
    @Autowired
    private IHouseService houseOriginService;

    private ModelMapper modelMapper = new ModelMapper();

    @GetMapping("/center")
    public String adminCenter() {
        return "admin/center";
    }

    @GetMapping("/welcome")
    public String adminWelcome() {
        return "admin/welcome";
    }

    @GetMapping("/login")
    public String adminLogin() {
        Authentication currentAuthentication = SecurityKit.getCurrentAuthentication();
        if (currentAuthentication != null) {
            Collection<? extends GrantedAuthority> authorities = currentAuthentication.getAuthorities();
            String permission = authorities.stream().map(e -> ((GrantedAuthority) e).getAuthority()).collect(Collectors.joining(","));
            if (permission.contains("ADMIN")) {
                return "redirect:/admin/center";
            } else {
                return "redirect:/user/center";
            }
        }
        return "admin/login";
    }

    @GetMapping("/add/house")
    public String addHouse() {
        return "admin/house-add";
    }

    @GetMapping("/house/list")
    public String houseList() {
        return "admin/house-list";
    }
    /**
     * 图片上传
     */
    @PostMapping(value = "/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse updateLoadImg(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM, null);
        }
        try {
            InputStream inputStream = file.getInputStream();
            Response response = this.qiniuService.uploadFile(inputStream);
            if (response.isOK()) {
                QiNiuVo qiNiuVo = gson.fromJson(response.bodyString(), QiNiuVo.class);
                return ApiResponse.ofSuccess(qiNiuVo);
            } else {
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        } catch (QiniuException e) {
            log.error(e.getMessage(), e);
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException ex) {
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR, null);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR, null);
    }


    @PostMapping("/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") com.youjian.xunwu.comm.form.HouseForm houseForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressVo> supportAddressVoMap = addressService.findSupportAddressByEnNameAndRegionName(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (supportAddressVoMap.entrySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM, null);
        }

        try {
            ServiceResult result = houseOriginService.save(houseForm);
            if (result.isSuccess()) {
                return ApiResponse.ofSuccess(result.getData());
            }else {
                return ApiResponse.ofMessage(HttpStatus.NOT_FOUND.value(), result.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * 房源浏览 admin
     */
    @PostMapping("/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DataTableSearch dataTableSearch) {

        Page<House> page = houseOriginService.findAllBy(dataTableSearch);
        List<HouseVo> vos = page.get().map(e -> {
            HouseVo vo = modelMapper.map(e, HouseVo.class);
            vo.setCover(this.qiniuConfig.getCdnPrefix() + vo.getCover());
            return vo;
        }).collect(Collectors.toList());
        ApiDataTableResponse apiDataTableResponse = new ApiDataTableResponse();
        apiDataTableResponse.setDraw(dataTableSearch.getDraw());
        apiDataTableResponse.setRecordsTotal(page.getTotalElements());
        apiDataTableResponse.setRecordsFiltered(page.getTotalElements());
        apiDataTableResponse.setData(vos);
        return apiDataTableResponse;
    }

    @GetMapping("/house/edit")
    public String houseEdit(@RequestParam("id") Long id, Model model) {

        if (id == null || id < 1) {
            return "404";
        }
        HouseVo vo = houseOriginService.findHouseById(id);
        if (vo == null) {
            return "404";
        }
        model.addAttribute("house", vo);
        Map<SupportAddress.Level, SupportAddressVo> address = addressService.findSupportAddressByEnNameAndRegionName(vo.getCityEnName(), vo.getRegionEnName());
        model.addAttribute("city", address.get(SupportAddress.Level.CITY));
        model.addAttribute("region", address.get(SupportAddress.Level.REGION));

        HouseDetailVo houseDetail = vo.getHouseDetail();
        SubwayVo subwayVo = addressService.findSubway(houseDetail.getSubwayLineId());
        if (subwayVo == null) {
            return "404";
        }

        SubwayStationVo subwayStationVo = addressService.findSubwayStationBySubwayStationId(houseDetail.getSubwayStationId());
        if (subwayStationVo == null) {
            return "404";
        }

        model.addAttribute("subway", subwayVo);
        model.addAttribute("station", subwayStationVo);

        return "admin/house-edit";
    }

    /** 设置房子图片封面 */
    @PostMapping("/house/cover")
    @ResponseBody
    public ApiResponse houseCover(@RequestParam("cover_id") Long coverId, @RequestParam("target_id") Long targetId) {
        if (coverId == null || targetId == null) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST, null);
        }
        HouseVo houseVo = houseOriginService.setHouseCover(coverId, targetId);
        if (houseVo == null) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST, null);
        }
        return ApiResponse.ofSuccess(houseVo);
    }

    @PostMapping("/house/edit")
    @ResponseBody
    public ApiResponse houseEdit(@Valid @ModelAttribute HouseForm houseForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        ServiceResult save = houseOriginService.update(houseForm);
        if (save.isSuccess()) {
            return ApiResponse.ofSuccess(save.getData());
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), save.getMessage());
    }

    @PutMapping("/house/operate/{houseId}/{code}")
    @ResponseBody
    public ApiResponse houseOperate(@PathVariable("houseId") Long houseId, @PathVariable("code") Integer operate) {

        ServiceResult<HouseVo> result = null;

        switch (operate) {
            case PASS:
                result = houseOriginService.updateStatus(houseId, HouseStatus.AUDITED);
                break;
            case PULL_OUT:
                result = houseOriginService.updateStatus(houseId, HouseStatus.UNREVIEWED);
                break;
            case RENT:
                result = houseOriginService.updateStatus(houseId, HouseStatus.RENTED);
                break;
            case DELETE:
                result = houseOriginService.updateStatus(houseId, HouseStatus.DELETED);
                break;
            default:
                return ApiResponse.ofStatus(ApiResponse.Status.NOT_SUPPORTED_OPERATION);
        }

        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getData());
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    @DeleteMapping("/house/tag")
    @ResponseBody
    public ApiResponse deleteTag(@RequestParam("house_id") Long houseId, @RequestParam("tag") String tag) {

        ServiceResult result = houseOriginService.deleteTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    @PostMapping("/house/tag")
    @ResponseBody
    public ApiResponse addTag(@RequestParam("house_id") Long houseId, @RequestParam("tag") String tag) {
        ServiceResult result = houseOriginService.addTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    @DeleteMapping("/house/photo")
    @ResponseBody
    public ApiResponse deletePicture(@RequestParam("id") Long pictureId) {
        ServiceResult result = null;
        try {
            result = houseOriginService.deletePic(pictureId);
            if (result.isSuccess()) {
                return ApiResponse.ofSuccess(null);
            } else {
                return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.ofMessage(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

}
