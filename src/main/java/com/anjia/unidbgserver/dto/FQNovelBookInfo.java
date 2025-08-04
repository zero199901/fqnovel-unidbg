package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * FQNovel 小说书籍信息
 */
@Data
public class FQNovelBookInfo {

    /**
     * 书籍ID
     */
    private String bookId;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 书籍描述
     */
    private String description;

    /**
     * 书籍封面URL
     */
    private String coverUrl;

    /**
     * 书籍状态 (0: 连载中, 1: 已完结)
     */
    private Integer status;

    /**
     * 章节总数
     */
    private Integer totalChapters;

    /**
     * 字数
     */
    private String wordNumber;

    /**
     * 标签
     */
    private String tags;

    /**
     * 最新章节标题
     */
    private String lastChapterTitle;
}
