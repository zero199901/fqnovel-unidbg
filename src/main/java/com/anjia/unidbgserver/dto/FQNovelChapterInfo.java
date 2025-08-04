package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQNovel 小说章节信息
 */
@Data
public class FQNovelChapterInfo {

    /**
     * 章节ID
     */
    private String chapterId;

    /**
     * 书籍ID
     */
    private String bookId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 原始章节内容-html
     */
    private String rawContent;

    /**
     * 章节序号
     */
    private Integer chapterIndex;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 上一章节ID
     */
    private String prevChapterId;

    /**
     * 下一章节ID
     */
    private String nextChapterId;

    /**
     * 是否免费章节
     */
    private Boolean isFree;

    /**
     * 纯文本内容（从HTML中提取的纯文本）
     */
    private String txtContent;
}
