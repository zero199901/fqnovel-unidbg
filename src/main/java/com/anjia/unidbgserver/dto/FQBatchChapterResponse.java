package com.anjia.unidbgserver.dto;

import lombok.Data;
import java.util.Map;

/**
 * 批量获取章节内容的响应
 */
@Data
public class FQBatchChapterResponse {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 书籍信息 (可选)
     */
    private FQNovelBookInfo bookInfo;
    
    /**
     * 章节内容映射
     * Key: 章节ID
     * Value: 章节详细信息(包含内容、标题、字数等)
     */
    private Map<String, FQBatchChapterInfo> chapters;
    
    /**
     * 请求的章节范围
     */
    private String requestedRange;
    
    /**
     * 成功获取的章节数量
     */
    private Integer successCount;
    
    /**
     * 请求的总章节数量
     */
    private Integer totalRequested;
    
    /**
     * 原始API响应数据 (可选, 仅当请求中rawResponse=true时返回)
     * 包含完整的API响应信息，用于调试和分析
     */
    private RawApiResponse rawApiResponse;
    
    /**
     * 原始API响应数据结构
     */
    @Data
    public static class RawApiResponse {
        /**
         * HTTP状态码
         */
        private Integer httpStatus;
        
        /**
         * 响应头信息
         */
        private java.util.Map<String, String> headers;
        
        /**
         * 原始响应体 (JSON字符串)
         */
        private String rawBody;
        
        /**
         * 响应体大小 (字节)
         */
        private Integer bodySize;
        
        /**
         * 是否为GZIP压缩
         */
        private Boolean isGzip;
        
        /**
         * 解压后的响应体 (如果原始是GZIP)
         */
        private String decompressedBody;
        
        /**
         * API请求URL
         */
        private String requestUrl;
        
        /**
         * 请求时间戳
         */
        private Long requestTimestamp;
        
        /**
         * 响应时间戳
         */
        private Long responseTimestamp;
    }
}