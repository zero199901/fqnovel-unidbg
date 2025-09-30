package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * 导出文本响应DTO
 */
@Data
public class FQExportTextResponse {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 书名
     */
    private String bookName;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 导出的章节数量
     */
    private Integer chapterCount;
    
    /**
     * 总字数
     */
    private Integer totalWords;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 导出时间戳
     */
    private Long exportTimestamp;
    
    /**
     * 文本内容（可选，如果内容太大可能为空）
     */
    private String content;
    
    /**
     * 是否内容被截断（超过限制时）
     */
    private Boolean contentTruncated = false;
}
