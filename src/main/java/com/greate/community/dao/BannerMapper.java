package com.greate.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.Banner;

import java.util.List;

@Mapper
public interface BannerMapper {
    List<Banner> findBannerAll();
}
