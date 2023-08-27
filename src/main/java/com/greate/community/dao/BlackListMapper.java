package com.greate.community.dao;

import com.greate.community.entity.BlackList;
import org.apache.ibatis.annotations.Mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface BlackListMapper {
    List<LinkedHashMap<String, Object>> findBlacklistByCondition(Map<String, Object> queryStrings);
    void addBlacklist(BlackList blackList);
}
