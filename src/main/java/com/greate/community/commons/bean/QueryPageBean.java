package com.greate.community.commons.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 封装查询条件
 *
 * @author YLM
 */
public class QueryPageBean implements Serializable {
    private static final long serialVersionUID = 106286014516173356L;

    private Integer currentPage;//页码
    private Integer pageSize;//每页记录数
    private Map<String, Object> queryStrings;//查询条件

    public QueryPageBean(Integer currentPage, Integer pageSize, Map<String, Object> queryStrings) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.queryStrings = queryStrings;
    }

    public QueryPageBean() {
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Map<String, Object> getQueryStrings() {
        return queryStrings;
    }

    public void setQueryStrings(Map<String, Object> queryString) {
        this.queryStrings = queryString;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

