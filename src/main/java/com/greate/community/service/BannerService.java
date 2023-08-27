package com.greate.community.service;

import com.greate.community.dao.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {
    @Autowired
    private BannerMapper bannerMapper;
    public List<Banner> findAllBanner(){
        return bannerMapper.findBannerAll();
    }
}
