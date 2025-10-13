package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * FQ书籍目录响应DTO - 基于实际API响应结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FQDirectoryResponse {

    /**
     * 是否需要重新获取Ban状态
     */
    @JsonProperty("ban_recover")
    private Boolean banRecover;

    /**
     * 附加数据列表
     */
    @JsonProperty("additional_item_data_list")
    private Object additionalItemDataList;

    /**
     * 目录数据 - 章节索引信息
     */
    @JsonProperty("catalog_data")
    private List<CatalogItem> catalogData;

    /**
     * 章节详细数据列表
     */
    @JsonProperty("item_data_list")
    private List<ItemData> itemDataList;

    /**
     * 字段缓存状态
     */
    @JsonProperty("field_cache_status")
    private FieldCacheStatus fieldCacheStatus;

    /**
     * 书籍信息
     */
    @JsonProperty("book_info")
    private FQNovelBookInfoResp bookInfo;

    /**
     * 连载数量
     */
    @JsonProperty("serial_count")
    private String serialCount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CatalogItem {

        /**
         * 目录ID
         */
        @JsonProperty("catalog_id")
        private String catalogId;

        /**
         * 目录标题
         */
        @JsonProperty("catalog_title")
        private String catalogTitle;

        /**
         * 章节ID
         */
        @JsonProperty("item_id")
        private String itemId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemData {

        /**
         * 章节ID
         */
        @JsonProperty("item_id")
        private String itemId;

        /**
         * 版本信息
         */
        private String version;

        /**
         * 内容MD5
         */
        @JsonProperty("content_md5")
        private String contentMd5;

        /**
         * 首次通过时间
         */
        @JsonProperty("first_pass_time")
        private Integer firstPassTime;

        /**
         * 章节标题
         */
        private String title;

        /**
         * 卷名
         */
        @JsonProperty("volume_name")
        private String volumeName;

        /**
         * 章节类型
         */
        @JsonProperty("chapter_type")
        private String chapterType;

        /**
         * 章节字数
         */
        @JsonProperty("chapter_word_number")
        private Integer chapterWordNumber;

        /**
         * 是否为审核章节
         */
        @JsonProperty("is_review")
        private Boolean isReview;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldCacheStatus {

        /**
         * 书籍信息缓存状态
         */
        @JsonProperty("book_info")
        private CacheInfo bookInfo;

        /**
         * 章节数据列表缓存状态
         */
        @JsonProperty("item_data_list")
        private CacheInfo itemDataList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CacheInfo {

        /**
         * 是否命中缓存
         */
        private Boolean hit;

        /**
         * MD5校验值
         */
        private String md5;
    }

}
