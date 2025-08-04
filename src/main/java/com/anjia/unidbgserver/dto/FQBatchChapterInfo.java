package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * 批量获取章节响应中的单个章节信息
 */
@Data
public class FQBatchChapterInfo {
    
    /**
     * 章节名称/标题
     */
    private String chapterName;
    
    /**
     * 原始内容 (HTML格式)
     */
    private String rawContent;
    
    /**
     * 纯文本内容
     */
    private String txtContent;
    
    /**
     * 字数
     */
    private Integer wordCount;
    
    /**
     * 章节序号 (可选)
     */
    private Integer chapterIndex;
    
    /**
     * 是否免费章节 (可选)
     */
    private Boolean isFree;
}