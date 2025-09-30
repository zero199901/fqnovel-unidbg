package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * 导出文本请求DTO
 */
@Data
public class FQExportTextRequest {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 书名（用于搜索获取bookId）
     */
    private String bookName;
    
    /**
     * 章节范围，格式如：1-100 或 1-1（单个章节）
     * 如果不指定，则导出所有章节
     */
    private String chapterRange;
    
    /**
     * 是否包含章节标题
     */
    private Boolean includeTitle = true;
    
    /**
     * 是否包含章节编号
     */
    private Boolean includeChapterNumber = true;
    
    /**
     * 章节间分隔符
     */
    private String separator = "\n\n";
    
    /**
     * 文件名（可选，不指定则自动生成）
     */
    private String fileName;
}
