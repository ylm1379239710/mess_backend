package com.greate.community.controller;

import com.greate.community.commons.result.ActionResult;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banner")
public class BannerController {
    @Autowired
    private BannerService bannerService;
    /**
     * 获取所有 Banner
     * @return
     */
    @GetMapping("/findAllBanner")
    public ActionResult findAllBanner() {
        List<Banner> allBanner = bannerService.findAllBanner();
        return ResultBuilder.buildActionSuccess(allBanner);
    }
}
