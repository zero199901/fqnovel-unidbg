package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQNovel API 通用响应
 */
@Data
public class FQNovelResponse<T> {
    
    /**
     * 响应码 (0: 成功, 其他: 失败)
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 服务器时间戳
     */
    private Long serverTime;
    
    public static <T> FQNovelResponse<T> success(T data) {
        FQNovelResponse<T> response = new FQNovelResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        response.setServerTime(System.currentTimeMillis());
        return response;
    }
    
    public static <T> FQNovelResponse<T> error(String message) {
        FQNovelResponse<T> response = new FQNovelResponse<>();
        response.setCode(-1);
        response.setMessage(message);
        response.setServerTime(System.currentTimeMillis());
        return response;
    }
    
    public static <T> FQNovelResponse<T> error(Integer code, String message) {
        FQNovelResponse<T> response = new FQNovelResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setServerTime(System.currentTimeMillis());
        return response;
    }
}