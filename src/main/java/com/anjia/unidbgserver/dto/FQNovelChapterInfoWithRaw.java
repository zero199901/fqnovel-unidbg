package com.anjia.unidbgserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 包含原始API响应的章节信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FQNovelChapterInfoWithRaw {
    
    /**
     * 章节信息
     */
    private FQNovelChapterInfo chapterInfo;
    
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
        private Integer httpStatus;
        private java.util.Map<String, String> headers;
        private String rawBody;
        private Integer bodySize;
        private Boolean isGzip;
        private String decompressedBody;
        private String requestUrl;
        private Long requestTimestamp;
        private Long responseTimestamp;
    }
}
