package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/*
  FQNovel 小说书籍信息响应
 */
public class FQNovelBookInfoResp {
    /**
     * 自定义总价格
     */
    @JsonProperty("custom_total_price")
    private String customTotalPrice;

    /**
     * 原始书籍名称
     */
    @JsonProperty("original_book_name")
    private String originalBookName;

    /**
     * 审核忽略标志
     */
    @JsonProperty("audit_ignore")
    private String auditIgnore;

    /**
     * 推荐数量级别
     */
    @JsonProperty("recommend_count_level")
    private String recommendCountLevel;

    /**
     * 数据评级
     */
    @JsonProperty("data_rate")
    private String dataRate;

    /**
     * 是否为故事CP
     */
    @JsonProperty("is_story_cp")
    private String isStoryCp;

    /**
     * TTS分发状态
     */
    @JsonProperty("tts_distribution")
    private String ttsDistribution;

    /**
     * 扩展缩略图URL
     */
    @JsonProperty("expand_thumb_url")
    private String expandThumbUrl;

    /**
     * 音频缩略图URI
     */
    @JsonProperty("audio_thumb_uri")
    private String audioThumbUri;

    /**
     * 总价格
     */
    @JsonProperty("total_price")
    private String totalPrice;

    /**
     * 加入书架数量
     */
    @JsonProperty("add_bookshelf_count")
    private String addBookshelfCount;

    /**
     * 小说书籍缩略图类型
     */
    @JsonProperty("novel_book_thumb_type")
    private String novelBookThumbType;

    /**
     * 编辑邀请标志
     */
    @JsonProperty("editor_invite_sign")
    private String editorInviteSign;

    /**
     * 风险率延迟入队时间戳
     */
    @JsonProperty("risk_rate_delay_enqueue_ts")
    private String riskRateDelayEnqueueTs;

    /**
     * 授权类型
     */
    @JsonProperty("authorize_type")
    private String authorizeType;

    /**
     * 分类
     */
    private String category;

    /**
     * MP性别
     */
    @JsonProperty("mp_gender")
    private String mpGender;

    /**
     * 书籍搜索可见性
     */
    @JsonProperty("book_search_visible")
    private String bookSearchVisible;

    /**
     * 折扣自定义总价格
     */
    @JsonProperty("discount_custom_total_price")
    private String discountCustomTotalPrice;

    /**
     * 区域可见性信息
     */
    @JsonProperty("region_visibility_info")
    private String regionVisibilityInfo;

    /**
     * 保持发布天数
     */
    @JsonProperty("keep_publish_days")
    private String keepPublishDays;

    /**
     * 纯分类标签
     */
    @JsonProperty("pure_category_tags")
    private String pureCategoryTags;

    /**
     * 第三方阅读历史
     */
    @JsonProperty("third_read_history")
    private String thirdReadHistory;

    /**
     * 长度类型
     */
    @JsonProperty("length_type")
    private String lengthType;

    /**
     * 作者ID
     */
    @JsonProperty("author_id")
    private String authorId;

    /**
     * 音频主色调
     */
    @JsonProperty("color_audio_most_popular")
    private String colorAudioMostPopular;

    /**
     * 最后章节项目ID
     */
    @JsonProperty("last_chapter_item_id")
    private String lastChapterItemId;

    /**
     * 真实章节顺序
     */
    @JsonProperty("real_chapter_order")
    private String realChapterOrder;

    /**
     * 相关音频书籍ID
     */
    @JsonProperty("related_audio_book_id")
    private String relatedAudioBookId;

    /**
     * 是否为电子书
     */
    @JsonProperty("is_ebook")
    private String isEbook;

    /**
     * 推荐审核员最后时间
     */
    @JsonProperty("recommend_auditor_last_time")
    private String recommendAuditorLastTime;

    /**
     * 出版社
     */
    private String press;

    /**
     * 总阅读数
     */
    @JsonProperty("read_count_all")
    private String readCountAll;

    /**
     * 音频主导色
     */
    @JsonProperty("color_audio_dominate")
    private String colorAudioDominate;

    /**
     * 音频启用随机播放
     */
    @JsonProperty("audio_enable_random_play")
    private Boolean audioEnableRandomPlay;

    /**
     * 评分
     */
    private String score;

    /**
     * 移除类型
     */
    @JsonProperty("remove_type")
    private String removeType;

    /**
     * 小说文本类型
     */
    @JsonProperty("novel_text_type")
    private String novelTextType;

    /**
     * 角色列表
     */
    private String roles;

    /**
     * 真实独家标志
     */
    @JsonProperty("real_exclusive")
    private String realExclusive;

    /**
     * 书籍ID
     */
    @JsonProperty("book_id")
    private String bookId;

    /**
     * 收听数量
     */
    @JsonProperty("listen_count")
    private String listenCount;

    /**
     * 分类FM
     */
    @JsonProperty("category_fm")
    private String categoryFm;

    /**
     * 媒体书籍首次推荐时间
     */
    @JsonProperty("media_book_first_recommend_time")
    private String mediaBookFirstRecommendTime;

    /**
     * 独家标志
     */
    private String exclusive;

    /**
     * 段落匹配信息
     */
    @JsonProperty("para_match_infos")
    private Map<String, Object> paraMatchInfos;

    /**
     * 关注ID
     */
    @JsonProperty("concern_id")
    private String concernId;

    /**
     * 插入协议类型
     */
    @JsonProperty("inset_agreement_type")
    private String insetAgreementType;

    /**
     * 阅读数量回退阈值
     */
    @JsonProperty("read_cnt_fall_back_threshold")
    private String readCntFallBackThreshold;

    /**
     * 首次可见时间
     */
    @JsonProperty("first_visible_time")
    private String firstVisibleTime;

    /**
     * 最新阅读时间
     */
    @JsonProperty("latest_read_time")
    private String latestReadTime;

    /**
     * 每日读者UV总和
     */
    @JsonProperty("reader_uv_sum_daily")
    private String readerUvSumDaily;

    /**
     * 作者信息
     */
    @JsonProperty("author_info")
    private AuthorInfo authorInfo;

    /**
     * 30天阅读数量
     */
    @JsonProperty("read_dcnt_30d")
    private String readDcnt30d;

    /**
     * 是否为新书
     */
    @JsonProperty("is_new")
    private String isNew;

    /**
     * 预估章节数
     */
    @JsonProperty("estimated_chapter_count")
    private String estimatedChapterCount;

    /**
     * 番茄书籍状态
     */
    @JsonProperty("tomato_book_status")
    private String tomatoBookStatus;

    /**
     * 类型类别
     */
    @JsonProperty("genre_type")
    private String genreType;

    /**
     * 版权信息
     */
    @JsonProperty("copyright_info")
    private String copyrightInfo;

    /**
     * 来源
     */
    private String source;

    /**
     * 原始作者ID列表
     */
    @JsonProperty("original_author_ids")
    private List<Long> originalAuthorIds;

    /**
     * 角色
     */
    private String role;

    /**
     * 人物
     */
    private String persona;

    /**
     * 产品ID
     */
    @JsonProperty("product_id")
    private String productId;

    /**
     * 免费状态
     */
    @JsonProperty("free_status")
    private String freeStatus;

    /**
     * 将保持更新天数
     */
    @JsonProperty("will_keep_update_days")
    private String willKeepUpdateDays;

    /**
     * 内容章节数
     */
    @JsonProperty("content_chapter_number")
    private String contentChapterNumber;

    /**
     * 分类V2
     */
    @JsonProperty("category_v2")
    private String categoryV2;

    /**
     * 风险率
     */
    @JsonProperty("risk_rate")
    private String riskRate;

    /**
     * 第一章标题
     */
    @JsonProperty("first_chapter_title")
    private String firstChapterTitle;

    /**
     * 从属媒体ID
     */
    @JsonProperty("slave_media_id")
    private String slaveMediaId;

    /**
     * 音调
     */
    private String tones;

    /**
     * VIP书籍
     */
    @JsonProperty("vip_book")
    private String vipBook;

    /**
     * 完整分类
     */
    @JsonProperty("complete_category")
    private String completeCategory;

    /**
     * 第一章组ID
     */
    @JsonProperty("first_chapter_group_id")
    private String firstChapterGroupId;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 书籍类型
     */
    @JsonProperty("book_type")
    private String bookType;

    /**
     * 阅读数量回退阈值
     */
    @JsonProperty("read_cnt_fallback_threshold")
    private String readCntFallbackThreshold;

    /**
     * 最后章节更新时间
     */
    @JsonProperty("last_chapter_update_time")
    private String lastChapterUpdateTime;

    /**
     * 主导色
     */
    @JsonProperty("color_dominate")
    private String colorDominate;

    /**
     * 折扣价格
     */
    @JsonProperty("discount_price")
    private String discountPrice;

    /**
     * 更新停止
     */
    @JsonProperty("update_stop")
    private String updateStop;

    /**
     * 14天加入书架数量
     */
    @JsonProperty("add_shelf_count_14d")
    private String addShelfCount14d;

    /**
     * 阅读数量回退子文本
     */
    @JsonProperty("read_cnt_fallback_sub_text")
    private String readCntFallbackSubText;

    /**
     * 可见性信息
     */
    @JsonProperty("visibility_info")
    private String visibilityInfo;

    /**
     * 销售状态
     */
    @JsonProperty("sale_status")
    private String saleStatus;

    /**
     * 操作缩略图URI
     */
    @JsonProperty("op_thumb_uri")
    private String opThumbUri;

    /**
     * 用户选择标志
     */
    @JsonProperty("flight_user_selected")
    private String flightUserSelected;

    /**
     * 字数
     */
    @JsonProperty("word_number")
    private String wordNumber;

    /**
     * 是否为老白
     */
    @JsonProperty("is_laobai")
    private String isLaobai;

    /**
     * 14天读者UV
     */
    @JsonProperty("reader_uv_14day")
    private String readerUv14day;

    /**
     * 隐藏收听球
     */
    @JsonProperty("hide_listen_ball")
    private Boolean hideListenBall;

    /**
     * 14天收听UV
     */
    @JsonProperty("listen_uv_14day")
    private String listenUv14day;

    /**
     * 创作状态
     */
    @JsonProperty("creation_status")
    private String creationStatus;

    /**
     * 书籍flight版本ID
     */
    @JsonProperty("book_flight_version_id")
    private String bookFlightVersionId;

    /**
     * 第三方合作伙伴URL
     */
    @JsonProperty("third_partern_url")
    private String thirdParternUrl;

    /**
     * 相关音频书籍ID列表
     */
    @JsonProperty("related_audio_bookids")
    private String relatedAudioBookids;

    /**
     * 阅读数量
     */
    @JsonProperty("read_count")
    private String readCount;

    /**
     * 缩略图URI
     */
    @JsonProperty("thumb_uri")
    private String thumbUri;

    /**
     * 30天收听UV
     */
    @JsonProperty("listen_uv_30day")
    private String listenUv30day;

    /**
     * 广告免费显示
     */
    @JsonProperty("ad_free_show")
    private String adFreeShow;

    /**
     * UG可启动应用ID列表
     */
    @JsonProperty("ug_launchable_appid_list")
    private String ugLaunchableAppidList;

    /**
     * 论坛ID
     */
    @JsonProperty("forum_id")
    private String forumId;

    /**
     * 禁用阅读器功能
     */
    @JsonProperty("disable_reader_feature")
    private Integer disableReaderFeature;

    /**
     * 子信息
     */
    @JsonProperty("sub_info")
    private String subInfo;

    /**
     * 水平缩略图URL
     */
    @JsonProperty("horiz_thumb_url")
    private String horizThumbUrl;

    /**
     * 分类模式
     */
    @JsonProperty("category_schema")
    private String categorySchema;

    /**
     * 类型
     */
    private String type;

    /**
     * 副标题
     */
    @JsonProperty("sub_title")
    private String subTitle;

    /**
     * 最新收听时间
     */
    @JsonProperty("latest_listen_time")
    private String latestListenTime;

    /**
     * 媒体ID
     */
    @JsonProperty("media_id")
    private String mediaId;

    /**
     * 类型
     */
    private String genre;

    /**
     * 平台
     */
    private String platform;

    /**
     * 10%完成率
     */
    @JsonProperty("finish_rate_10")
    private String finishRate10;

    /**
     * 时长
     */
    private String duration;

    /**
     * 原始策略
     */
    @JsonProperty("origin_strategy")
    private String originStrategy;

    /**
     * 绑定作者ID
     */
    @JsonProperty("bind_author_ids")
    private String bindAuthorIds;

    /**
     * 原始应用级别日期
     */
    @JsonProperty("origin_app_level_date")
    private String originAppLevelDate;

    /**
     * 作者
     */
    private String author;

    /**
     * 所有书架数量
     */
    @JsonProperty("all_bookshelf_count")
    private String allBookshelfCount;

    /**
     * 销售类型
     */
    @JsonProperty("sale_type")
    private String saleType;

    /**
     * 阅读数量文本
     */
    @JsonProperty("read_cnt_text")
    private String readCntText;

    /**
     * 连载数量
     */
    @JsonProperty("serial_count")
    private String serialCount;

    /**
     * 收听进度
     */
    @JsonProperty("listen_progress")
    private String listenProgress;

    /**
     * 书架数量历史
     */
    @JsonProperty("shelf_cnt_history")
    private String shelfCntHistory;

    /**
     * 子摘要
     */
    @JsonProperty("sub_abstract")
    private String subAbstract;

    /**
     * 分类V2 ID列表
     */
    @JsonProperty("category_v2_ids")
    private String categoryV2Ids;

    /**
     * 标签
     */
    private String tags;

    /**
     * 更新状态
     */
    @JsonProperty("update_status")
    private String updateStatus;

    /**
     * 最后发布时间
     */
    @JsonProperty("last_publish_time")
    private String lastPublishTime;

    /**
     * 平台书籍ID
     */
    @JsonProperty("platform_book_id")
    private String platformBookId;

    /**
     * 操作频道
     */
    @JsonProperty("op_channel")
    private String opChannel;

    /**
     * 原始应用级别
     */
    @JsonProperty("origin_app_level")
    private String originAppLevel;

    /**
     * 发布日期
     */
    @JsonProperty("published_date")
    private String publishedDate;

    /**
     * 最后章节首次通过时间
     */
    @JsonProperty("last_chapter_first_pass_time")
    private String lastChapterFirstPassTime;

    /**
     * 第一章项目ID
     */
    @JsonProperty("first_chapter_item_id")
    private String firstChapterItemId;

    /**
     * 阅读礼物权限
     */
    @JsonProperty("reading_gift_permission")
    private String readingGiftPermission;

    /**
     * 使用方形图片
     */
    @JsonProperty("use_square_pic")
    private String useSquarePic;

    /**
     * 高清音频缩略图URL
     */
    @JsonProperty("audio_thumb_url_hd")
    private String audioThumbUrlHd;

    /**
     * 最新阅读项目ID
     */
    @JsonProperty("latest_read_item_id")
    private String latestReadItemId;

    /**
     * 海报信息
     */
    @JsonProperty("poster_info")
    private String posterInfo;

    /**
     * 最后章节标题
     */
    @JsonProperty("last_chapter_title")
    private String lastChapterTitle;

    /**
     * 缩略图URL
     */
    @JsonProperty("thumb_url")
    private String thumbUrl;

    /**
     * 子类型
     */
    @JsonProperty("sub_genre")
    private String subGenre;

    /**
     * 缩略图确认状态
     */
    @JsonProperty("thumb_confirm_status")
    private String thumbConfirmStatus;

    /**
     * 礼物权限
     */
    @JsonProperty("gift_permission")
    private String giftPermission;

    /**
     * 最后章节组ID
     */
    @JsonProperty("last_chapter_group_id")
    private String lastChapterGroupId;

    /**
     * 重复内容
     */
    @JsonProperty("duplicate_content")
    private String duplicateContent;

    /**
     * 最新收听项目ID
     */
    @JsonProperty("latest_listen_item_id")
    private String latestListenItemId;

    /**
     * 书籍摘要V2
     */
    @JsonProperty("book_abstract_v2")
    private String bookAbstractV2;

    /**
     * 摘要
     */
    @JsonProperty("abstract")
    private String abstractContent;

    /**
     * 海报ID
     */
    @JsonProperty("poster_id")
    private String posterId;

    /**
     * 签名申请时间
     */
    @JsonProperty("sign_apply_time")
    private String signApplyTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 原始默认书籍
     */
    @JsonProperty("origin_default_book")
    private String originDefaultBook;

    /**
     * 沐叶编辑分类
     */
    @JsonProperty("muye_editor_category")
    private String muyeEditorCategory;

    /**
     * 原始作者列表
     */
    @JsonProperty("original_authors")
    private String originalAuthors;

    /**
     * 是否有匹配的音频书籍
     */
    @JsonProperty("has_match_audio_books")
    private String hasMatchAudioBooks;

    /**
     * 收益时间
     */
    @JsonProperty("benefit_time")
    private String benefitTime;

    /**
     * 头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * 最受欢迎色彩
     */
    @JsonProperty("color_most_popular")
    private String colorMostPopular;

    /**
     * 阅读进度
     */
    @JsonProperty("read_progress")
    private String readProgress;

    /**
     * 规则排名分数
     */
    @JsonProperty("rule_rank_score")
    private String ruleRankScore;

    /**
     * TTS推荐屏蔽
     */
    @JsonProperty("tts_rec_block")
    private String ttsRecBlock;

    /**
     * 基础价格
     */
    @JsonProperty("base_price")
    private String basePrice;

    /**
     * 阅读数量回退文本
     */
    @JsonProperty("read_cnt_fall_back_text")
    private String readCntFallBackText;

    /**
     * 推荐审核员状态
     */
    @JsonProperty("recommend_auditor_status")
    private String recommendAuditorStatus;

    /**
     * 首次上线时间
     */
    @JsonProperty("first_online_time")
    private String firstOnlineTime;

    /**
     * 原始活动标志
     */
    @JsonProperty("origin_activity_flag")
    private String originActivityFlag;

    /**
     * TTS状态
     */
    @JsonProperty("tts_status")
    private String ttsStatus;

    /**
     * 礼物词语
     */
    @JsonProperty("gift_word")
    private String giftWord;

    /**
     * 书籍名称
     */
    @JsonProperty("book_name")
    private String bookName;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private String createTime;

    /**
     * 阅读页面进度
     */
    @JsonProperty("read_page_progress")
    private String readPageProgress;

    /**
     * 性别
     */
    private String gender;

    /**
     * 操作书籍名称
     */
    @JsonProperty("op_book_name")
    private String opBookName;

    /**
     * 所有权类型
     */
    @JsonProperty("ownership_type")
    private String ownershipType;

    /**
     * 合同授权
     */
    @JsonProperty("contract_authorize")
    private String contractAuthorize;

    /**
     * 操作标签
     */
    @JsonProperty("op_tag")
    private String opTag;

    /**
     * 外部UID
     */
    @JsonProperty("external_uid")
    private String externalUid;

    /**
     * 赞扬支持权限
     */
    @JsonProperty("praise_support_permission")
    private String praiseSupportPermission;

    /**
     * 详情页缩略图URL
     */
    @JsonProperty("detail_page_thumb_url")
    private String detailPageThumbUrl;

    /**
     * 出版商
     */
    private String publisher;

    /**
     * 主要冷启动
     */
    @JsonProperty("major_coldstart")
    private String majorColdstart;

    /**
     * 首次通过项目
     */
    @JsonProperty("first_pass_item")
    private String firstPassItem;

    /**
     * 保持更新天数
     */
    @JsonProperty("keep_update_days")
    private String keepUpdateDays;

    // 新增字段，根据响应体补充

    /**
     * 相关书籍ID
     */
    @JsonProperty("related_bookids")
    private String relatedBookids;

    /**
     * 绑定声誉书籍ID
     */
    @JsonProperty("bind_reputation_book_id")
    private String bindReputationBookId;

    /**
     * 声誉最新设置时间
     */
    @JsonProperty("reputation_latest_set_time")
    private String reputationLatestSetTime;

    /**
     * 额外字数
     */
    @JsonProperty("extra_word_number")
    private String extraWordNumber;

    /**
     * 海报标志
     */
    @JsonProperty("poster_flag")
    private String posterFlag;

    /**
     * 书籍flight别名缩略图
     */
    @JsonProperty("book_flight_alias_thumb")
    private String bookFlightAliasThumb;

    /**
     * 修改声誉书籍名称
     */
    @JsonProperty("modified_reputation_book_name")
    private String modifiedReputationBookName;

    /**
     * 书籍flight别名名称
     */
    @JsonProperty("book_flight_alias_name")
    private String bookFlightAliasName;

    /**
     * 作者修改章节开关
     */
    @JsonProperty("author_modify_chapter_switch")
    private String authorModifyChapterSwitch;

    /**
     * 声誉缩略图URI
     */
    @JsonProperty("reputation_thumb_uri")
    private String reputationThumbUri;

    /**
     * 长度分数
     */
    @JsonProperty("changdu_profile_score")
    private String changduProfileScore;

    /**
     * 写作额外权限
     */
    @JsonProperty("write_extra_permission")
    private String writeExtraPermission;

    /**
     * 创作最新完成时间
     */
    @JsonProperty("creation_latest_finish_time")
    private String creationLatestFinishTime;

    /**
     * 是否有额外章节
     */
    @JsonProperty("has_extra_chapter")
    private String hasExtraChapter;

    /**
     * Flight标志
     */
    @JsonProperty("flight_flag")
    private String flightFlag;

    /**
     * 书籍简称
     */
    @JsonProperty("book_short_name")
    private String bookShortName;

    /**
     * 声誉审核状态
     */
    @JsonProperty("reputation_audit_status")
    private String reputationAuditStatus;

    /**
     * 作者信息内部类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorInfo {
        /**
         * 用户ID
         */
        @JsonProperty("user_id")
        private String userId;

        /**
         * 用户类型
         */
        @JsonProperty("user_type")
        private Integer userType;

        /**
         * 用户名
         */
        @JsonProperty("user_name")
        private String userName;

        /**
         * 用户头像
         */
        @JsonProperty("user_avatar")
        private String userAvatar;

        /**
         * 是否为作者
         */
        @JsonProperty("is_author")
        private Boolean isAuthor;

        /**
         * 性别
         */
        private Integer gender;

        /**
         * 描述
         */
        private String description;

        /**
         * 是否为VIP
         */
        @JsonProperty("is_vip")
        private Boolean isVip;

        /**
         * 是否官方认证
         */
        @JsonProperty("is_official_cert")
        private Boolean isOfficialCert;

        /**
         * 是否有徽章
         */
        @JsonProperty("has_medal")
        private Boolean hasMedal;

        /**
         * 作者类型
         */
        @JsonProperty("author_type")
        private Integer authorType;

        /**
         * 关系类型
         */
        @JsonProperty("relation_type")
        private Integer relationType;

        /**
         * 是否可以关注
         */
        @JsonProperty("can_follow")
        private Boolean canFollow;

        /**
         * 是否已取消
         */
        @JsonProperty("is_cancelled")
        private Boolean isCancelled;

        /**
         * 粉丝模式
         */
        @JsonProperty("fans_schema")
        private String fansSchema;

        /**
         * 关注模式
         */
        @JsonProperty("follow_schema")
        private String followSchema;

        /**
         * 编码用户ID
         */
        @JsonProperty("encode_user_id")
        private String encodeUserId;

        /**
         * UGC阅读书籍数量
         */
        @JsonProperty("ugc_read_book_count")
        private Integer ugcReadBookCount;

        /**
         * 个人资料性别
         */
        @JsonProperty("profile_gender")
        private Integer profileGender;

        /**
         * 扩展用户头像
         */
        @JsonProperty("expand_user_avatar")
        private String expandUserAvatar;
    }
}
