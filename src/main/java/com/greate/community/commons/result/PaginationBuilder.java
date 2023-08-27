package com.greate.community.commons.result;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构建分页信息
 */
public class PaginationBuilder {

    private PaginationBuilder() {

    }

    public static Map<String, Object> buildResult(List<LinkedHashMap<String, Object>> resultList, long total, Integer currentPage, Integer pageSize) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("list", resultList);
        LinkedHashMap<String, Integer> paginationMap = new LinkedHashMap<>();
        paginationMap.put("total", (int) total);
        paginationMap.put("pageSize", pageSize);
        paginationMap.put("current", currentPage);
        resultMap.put("pagination", paginationMap);
        return resultMap;
    }

    /**
     * 构造T返回结构
     *
     * @param resultList  未知类型List
     * @param total       总页数
     * @param currentPage 当前页
     * @param pageSize    每页数量
     * @return Map<String, Object>
     */
    public static Map<String, Object> buildTResult(List<?> resultList, long total, Integer currentPage, Integer pageSize) {
        LinkedHashMap<String, Object> resultTMap = new LinkedHashMap<>();
        resultTMap.put("list", resultList);
        LinkedHashMap<String, Integer> paginationMap = new LinkedHashMap<>();
        paginationMap.put("total", (int) total);
        paginationMap.put("pageSize", pageSize);
        paginationMap.put("current", currentPage);
        resultTMap.put("pagination", paginationMap);
        return resultTMap;
    }

}