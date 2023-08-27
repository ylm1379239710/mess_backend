package com.greate.community.commons.result;

import java.io.Serializable;

/**
 * HTTP的返回响应类
 */
public class ListResult<T> implements Serializable {

    private static final long serialVersionUID = -3755143195269176337L;

    /**
     * 状态编码
     */
    private int status;

    /**
     * 数据 NOSONAR
     */
    private T data;

    /**
     * 消息
     */
    private String message;

    public ListResult() {
        super();
    }

    public ListResult(int status, T data, String message) {
        super();
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}