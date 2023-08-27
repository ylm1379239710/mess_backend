package com.greate.community.commons.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 构建结果返回信息
 */
public class ResultBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultBuilder.class);
    private static final String SUCCESS = "success";
    private static final String Fail = "fail";
    private static ListResult<Object> listResult = new ListResult<>();
    private static ActionResult actionResult = new ActionResult();

    private ResultBuilder() {

    }

    private static ListResult<Object> getObjectListResult(Object data) {
        try {
            listResult = new ListResult<>();
            listResult.setData(data);
            listResult.setStatus(HttpStatus.OK.value());
            listResult.setMessage(SUCCESS);
        } catch (Exception e) {
            ResultBuilder.buildListWarn(listResult, e);
        }
        return listResult;
    }

    public static ListResult<Object> buildListSuccess(Object data) {
        return getObjectListResult(data);
    }

    public static ListResult<Object> buildListSuccess(Map<String, Object> data) {
        return getObjectListResult(data);
    }

    public static ListResult<Object> buildListSuccess(List<?> data) {
        return getObjectListResult(data);
    }

    public static void buildListWarn(ListResult<Object> result, Exception e) {
        result.setData(Collections.emptyMap());
        result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setMessage(e.getMessage());
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(e.toString());
        }
    }

    public static ActionResult buildActionSuccess() {
        try {
            actionResult = new ActionResult();
            actionResult.setStatus(HttpStatus.OK.value());
            actionResult.setMessage(SUCCESS);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }

    public static ActionResult buildActionFail() {
        try {
            actionResult = new ActionResult();
            actionResult.setStatus(HttpStatus.ACCEPTED.value());
            actionResult.setMessage(Fail);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }
    public static ActionResult buildActionFail(Object data,Integer status) {
        try {
            actionResult = new ActionResult();
            actionResult.setData(data);
            actionResult.setStatus(status);
            actionResult.setMessage(Fail);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }
    public static ActionResult buildActionFail(Object data,String message) {
        try {
            actionResult = new ActionResult();
            actionResult.setData(data);
            actionResult.setStatus(HttpStatus.ACCEPTED.value());
            actionResult.setMessage(message);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }

    public static ActionResult buildActionFail(Integer status, Object data,String message) {
        try {
            actionResult = new ActionResult();
            actionResult.setData(data);
            actionResult.setStatus(status);
            actionResult.setMessage(message);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }

    public static ActionResult buildActionSuccess(Object data) {
        try {
            actionResult = new ActionResult();
            actionResult.setData(data);
            actionResult.setStatus(HttpStatus.OK.value());
            actionResult.setMessage(SUCCESS);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }

    public static ActionResult buildActionSuccess(Object data,String message) {
        try {
            actionResult = new ActionResult();
            actionResult.setData(data);
            actionResult.setStatus(HttpStatus.OK.value());
            actionResult.setMessage(message);
        } catch (Exception e) {
            ResultBuilder.buildActionWarn(actionResult, e);
        }
        return actionResult;
    }

    public static void buildActionWarn(ActionResult result, Exception e) {
        result.setStatus(HttpStatus.SEE_OTHER.value());
        result.setMessage(e.getMessage());
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(e.toString());
        }
    }

}