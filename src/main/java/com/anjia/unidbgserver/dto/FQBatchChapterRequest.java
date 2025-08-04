package com.anjia.unidbgserver.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量获取章节内容的请求参数
 */
@Data
public class FQBatchChapterRequest {

    /**
     * 书籍ID (必须)
     */
    private String bookId;

    /**
     * 章节范围，例如 "1-30" 或 "5-10"
     * 如果只获取单个章节，可以是 "5" 或 "5-5"
     */
    private String chapterRange;

    /**
     * 章节ID列表 (可选, 如果提供了此字段，则chapterRange将被忽略)
     * 用于指定需要获取的具体章节ID
     */
    private List<String> chapterIds;

    /**
     * 设备ID (可选)
     */
    private String deviceId;

    /**
     * 应用ID (可选)
     */
    private String iid;

    /**
     * 用户token (可选, 获取付费章节时需要)
     */
    private String token;

    /**
     * 附加的请求头参数 (可选)
     */
    private java.util.Map<String, String> extraHeaders;
}
