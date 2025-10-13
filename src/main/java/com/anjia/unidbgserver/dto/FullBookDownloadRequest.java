package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 全本下载请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullBookDownloadRequest {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 每批获取的章节数量
     */
    @Builder.Default
    private Integer batchSize = 30;
    
    /**
     * 是否保存到Redis
     */
    @Builder.Default
    private Boolean saveToRedis = true;
    
    /**
     * 是否流式返回
     */
    @Builder.Default
    private Boolean streamResponse = true;
    
    /**
     * 开始章节索引（从0开始）
     */
    @Builder.Default
    private Integer startIndex = 0;
    
    /**
     * 最大章节数量（0表示获取全部）
     */
    @Builder.Default
    private Integer maxChapters = 0;
}
