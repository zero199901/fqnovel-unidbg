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
}