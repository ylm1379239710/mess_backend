package com.greate.community.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.greate.community.commons.result.PaginationBuilder;
import com.greate.community.dao.BlackListMapper;
import com.greate.community.entity.BlackList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlackListService {
    @Autowired
    BlackListMapper blackListMapper;
    /**
     * 根据条件查询交易信息
     * @param currentPage
     * @param pageSize
     * @param queryStrings
     * @return
     */
    public Map<String, Object> findBlacklistByCondition(Integer currentPage, Integer pageSize, Map<String, Object> queryStrings) {
        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        List<LinkedHashMap<String, Object>> resultList = blackListMapper.findBlacklistByCondition(queryStrings);
        return PaginationBuilder.buildResult(resultList, page.getTotal(), currentPage, pageSize);
    }

    public void addBlackList(BlackList blackList) {
        blackList.setCreateTime(new Date());
        blackList.setIsAudit(0);
        blackListMapper.addBlacklist(blackList);
    }
}
