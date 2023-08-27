package com.greate.community.controller;

import com.greate.community.commons.bean.QueryPageBean;
import com.greate.community.commons.result.ActionResult;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.entity.BlackList;
import com.greate.community.service.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交易黑名单
 */
@RestController
@RequestMapping("/blackList")
public class BlackListController {
    @Autowired
    BlackListService blackListService;

    /**
     * 根据条件搜索  交易黑名单
     * @param queryPageBean
     * @return
     */
    @PostMapping("/findBlackListByCondition")
    public ActionResult findBlackListByCondition(@RequestBody QueryPageBean queryPageBean) {
        Map<String, Object> blacklistByCondition = blackListService.findBlacklistByCondition(queryPageBean.getCurrentPage(), queryPageBean.getPageSize(), queryPageBean.getQueryStrings());
        return ResultBuilder.buildActionSuccess(blacklistByCondition);
    }

    /**
     * 根据条件搜索  交易黑名单
     * @param blackList
     * @return
     */
    @PostMapping("/addBlackList")
    public ActionResult addBlackList(@RequestBody BlackList blackList) {
        blackListService.addBlackList(blackList);
        return ResultBuilder.buildActionSuccess(null,"挂榜信息已发送，等待审核");
    }
}
