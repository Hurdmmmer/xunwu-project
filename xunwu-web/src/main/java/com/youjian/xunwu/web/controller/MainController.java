package com.youjian.xunwu.web.controller;

import com.youjian.xunwu.comm.basic.ApiResponse;
import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.entity.SupportAddress;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.comm.form.RentValueBlock;
import com.youjian.xunwu.comm.utils.JsonMapper;
import com.youjian.xunwu.comm.vo.HouseVo;
import com.youjian.xunwu.comm.vo.ServiceResult;
import com.youjian.xunwu.comm.vo.SupportAddressVo;
import com.youjian.xunwu.comm.vo.UserVo;
import com.youjian.xunwu.search.service.ISearchService;
import com.youjian.xunwu.service.IAddressService;
import com.youjian.xunwu.service.IHouseService;
import com.youjian.xunwu.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 普通用户访问的 controller
 */
@Slf4j
@Controller
@RequestMapping("/")
public class MainController {

    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ISearchService searchService;

    /**
     * 搜索框自动补全功能
     */
    @GetMapping("/rent/house/autocomplete")
    @ResponseBody
    public ApiResponse autoComplete(@RequestParam("prefix") String prefix) {
        if (prefix.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult<List<String>> suggest = searchService.suggest(prefix);
        if (suggest.isSuccess()) {
            return ApiResponse.ofSuccess(suggest.getData());
        }

        return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
    }


    @GetMapping({"/", "/index"})
    public String index(@RequestParam("msg") String msg, Model model) {
        if (msg != null && msg.trim().length() > 0) {
            model.addAttribute("msg", msg);
        }
        return "index";
    }

    @GetMapping("/user/login")
    public String userLogin() {
        return "user/login";
    }


    @GetMapping("/user/center")
    public String userCenter() {
        return "user/center";
    }

    @GetMapping("/logout/page")
    public String logout() {
        return "logout";
    }


    @GetMapping("/403")
    public String error403() {
        return "403";
    }

    /**
     * 普通用户房源浏览处理器页面
     */
    @GetMapping("/rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch, Model mv, RedirectAttributes redirectAttributes,
                                HttpSession session) {
        log.info("rent house 查询条件为: {}", JsonMapper.obj2Json(rentSearch));
        if (rentSearch.getCityEnName() == null) {
            String cityEnName = (String) session.getAttribute("cityEnName");
            if (cityEnName == null) {
                log.info("没有传入城市信息...");
                redirectAttributes.addAttribute("msg", "must_chose_city");
                // 跳转到主页
                return "redirect:/createOrUpdateIndex";
            } else {
                rentSearch.setCityEnName(cityEnName);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        List<SupportAddressVo> supportAddressVos = addressService.findAllRegionByCityName(rentSearch.getCityEnName());

        if (supportAddressVos == null || supportAddressVos.size() == 0) {
            log.info("没有支持的区域: {}", rentSearch.getCityEnName());
            redirectAttributes.addAttribute("msg", "not_support_city");
            return "redirect:/createOrUpdateIndex";
        }

        ServiceMultiResult<List<HouseVo>> query = houseService.query(rentSearch);

        mv.addAttribute("total", query.getTotal());
        mv.addAttribute("houses", query.getData() == null ? new ArrayList<>() : query.getData());


        if (rentSearch.getRegionEnName() == null) {
            // 如果没传区域信息, 则表示全部查询
            rentSearch.setRegionEnName("*");
        }
        SupportAddressVo currentCity = addressService.findCityByEnName(rentSearch.getCityEnName());
        mv.addAttribute("searchBody", rentSearch);
        mv.addAttribute("regions", supportAddressVos);
        mv.addAttribute("currentCity", currentCity);

        mv.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        mv.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        // 翻页后保留当前选择的区间
        mv.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        mv.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    @GetMapping("/rent/house/show/{id}")
    public String houseShow(@PathVariable("id") Long id, Model model) {

        HouseVo vo = houseService.findHouseById(id);
        model.addAttribute("house", vo);
        String cityEnName = vo.getCityEnName();
        SupportAddressVo cityByEnName = addressService.findCityByEnName(cityEnName);
        model.addAttribute("city", cityByEnName);
        SupportAddressVo region = addressService.findRegionByEnName(vo.getRegionEnName());
        if (region == null) {
            return "redirect:/createOrUpdateIndex?msg=must_chose_city";
        }
        model.addAttribute("region", region);
        ServiceResult<UserVo> userDTOServiceResult = userService.findById(vo.getAdminId());
        model.addAttribute("agent", userDTOServiceResult.getData());
        // 统计一个小区中多少套房屋出租
        ServiceResult<Long> longServiceResult = searchService.aggregateDistrictHouse(cityEnName, region.getEnName(), vo.getDistrict());

        model.addAttribute("houseCountInDistrict", longServiceResult.getData());

        return "house-detail";
    }

}
