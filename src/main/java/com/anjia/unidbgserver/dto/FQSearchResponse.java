package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * FQ搜索响应DTO
 */
@Data
public class FQSearchResponse {
    /**
     * 书籍列表
     */
    private List<BookItem> books;

    /**
     * 总数量
     */
    private Integer total;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * 搜索ID
     */
    private String searchId;

    @Data
    public static class BookItem {

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
         * 描述
         */
        private String description;

        /**
         * 封面URL
         */
        private String coverUrl;

        /**
         * 分类
         */
        private String category;

        /**
         * 标签
         */
        private List<String> tags;

        /**
         * 字数
         */
        private Long wordCount;

        /**
         * 状态 (连载中/已完结等)
         */
        private String status;

        /**
         * 评分
         */
        private Double rating;

        /**
         * 更新时间
         */
        private Long updateTime;

        /**
         * 最新章节标题
         */
        private String lastChapterTitle;
    }
}
