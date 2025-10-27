package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * FQ搜索响应DTO（完整版）
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

    /**
     * 搜索结果书籍项（完整字段版本）
     */
    @Data
    public static class BookItem {

        // ============ 基础信息 ============
        
        /**
         * 书籍ID
         */
        private String bookId;

        /**
         * 书籍名称
         */
        private String bookName;

        /**
         * 书籍简称
         */
        private String bookShortName;

        /**
         * 作者
         */
        private String author;

        /**
         * 作者ID
         */
        private String authorId;

        /**
         * 作者信息
         */
        private Map<String, Object> authorInfo;

        /**
         * 描述
         */
        private String description;

        /**
         * 书籍摘要V2
         */
        private String bookAbstractV2;

        /**
         * 封面URL
         */
        private String coverUrl;

        /**
         * 详情页缩略图URL
         */
        private String detailPageThumbUrl;

        /**
         * 扩展缩略图URL
         */
        private String expandThumbUrl;

        /**
         * 水平缩略图URL
         */
        private String horizThumbUrl;

        /**
         * 状态 (连载中/已完结等)
         */
        private String status;

        /**
         * 创作状态
         */
        private String creationStatus;

        /**
         * 更新状态
         */
        private String updateStatus;

        // ============ 章节信息 ============

        /**
         * 字数
         */
        private Long wordCount;

        /**
         * 章节总数
         */
        private Integer totalChapters;

        /**
         * 第一章标题
         */
        private String firstChapterTitle;

        /**
         * 第一章ID
         */
        private String firstChapterItemId;

        /**
         * 最新章节标题
         */
        private String lastChapterTitle;

        /**
         * 最后章节ID
         */
        private String lastChapterItemId;

        /**
         * 更新时间
         */
        private Long updateTime;

        /**
         * 最后章节更新时间
         */
        private String lastChapterUpdateTime;

        // ============ 分类信息 ============

        /**
         * 分类
         */
        private String category;

        /**
         * 分类V2
         */
        private String categoryV2;

        /**
         * 完整分类
         */
        private String completeCategory;

        /**
         * 类型
         */
        private String genre;

        /**
         * 子类型
         */
        private String subGenre;

        /**
         * 标签
         */
        private List<String> tags;

        /**
         * 标签字符串
         */
        private String tagsStr;

        /**
         * 性别
         */
        private String gender;

        // ============ 统计数据 ============

        /**
         * 评分
         */
        private Double rating;

        /**
         * 阅读数量
         */
        private String readCount;

        /**
         * 阅读数量文本
         */
        private String readCntText;

        /**
         * 加入书架数量
         */
        private String addBookshelfCount;

        /**
         * 14天读者UV
         */
        private String readerUv14day;

        /**
         * 收听数量
         */
        private String listenCount;

        /**
         * 完成率10%
         */
        private String finishRate10;

        // ============ 价格与销售 ============

        /**
         * 总价格
         */
        private String totalPrice;

        /**
         * 基础价格
         */
        private String basePrice;

        /**
         * 折扣价格
         */
        private String discountPrice;

        /**
         * 免费状态
         */
        private String freeStatus;

        /**
         * VIP书籍
         */
        private String vipBook;

        // ============ 授权与版权 ============

        /**
         * 独家标志
         */
        private String exclusive;

        /**
         * 真实独家标志
         */
        private String realExclusive;

        /**
         * 版权信息
         */
        private String copyrightInfo;

        // ============ 显示与颜色 ============

        /**
         * 主导色
         */
        private String colorDominate;

        /**
         * 最受欢迎色彩
         */
        private String colorMostPopular;

        /**
         * 缩略图URI
         */
        private String thumbUri;

        // ============ 时间信息 ============

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 发布日期
         */
        private String publishedDate;

        /**
         * 最后发布时间
         */
        private String lastPublishTime;

        /**
         * 首次上线时间
         */
        private String firstOnlineTime;

        // ============ 书籍类型 ============

        /**
         * 书籍类型
         */
        private String bookType;

        /**
         * 是否为新书
         */
        private String isNew;

        /**
         * 是否为电子书
         */
        private String isEbook;

        /**
         * 长度类型
         */
        private String lengthType;

        // ============ 其他信息 ============

        /**
         * 书籍搜索可见性
         */
        private String bookSearchVisible;

        /**
         * 出版社
         */
        private String press;

        /**
         * 出版商
         */
        private String publisher;

        /**
         * ISBN
         */
        private String isbn;

        /**
         * 来源
         */
        private String source;

        /**
         * 平台
         */
        private String platform;

        /**
         * Flight标志
         */
        private String flightFlag;

        /**
         * 推荐数量级别
         */
        private String recommendCountLevel;

        /**
         * 数据评级
         */
        private String dataRate;

        /**
         * 风险率
         */
        private String riskRate;
    }
}
