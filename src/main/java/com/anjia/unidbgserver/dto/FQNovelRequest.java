package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQNovel API 请求参数
 */
@Data
public class FQNovelRequest {
    
    /**
     * 书籍ID (获取章节内容时必须)
     */
    private String bookId;
    
    /**
     * 章节ID (获取章节内容时必须)
     */
    private String chapterId;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 应用ID
     */
    private String iid;
    
    /**
     * 用户token (可选, 获取付费章节时需要)
     */
    private String token;
    
    /**
     * 附加的请求头参数
     */
    private java.util.Map<String, String> extraHeaders;
    
    /**
     * 是否返回原始API响应 (可选, 默认false)
     * 当设置为true时，响应中会包含原始API的响应数据，用于调试
     */
    private Boolean rawResponse = false;
}