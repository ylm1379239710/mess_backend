package com.greate.community.commons.result;

import java.io.Serializable;

/**
 * HTTP的返回响应类
 */
public class ActionResult implements Serializable {

    private static final long serialVersionUID = -8041820346732720877L;

    /**
     * 状态编码
     */
    private int status;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object data;

    public ActionResult() {
        super();
    }

    public ActionResult(int status, String message, Object data) {
        super();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}