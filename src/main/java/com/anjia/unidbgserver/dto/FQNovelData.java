package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * FQ小说数据DTO - 包含章节详细信息
 * 对应API响应中的novel_data字段
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FQNovelData {

    /**
     * UG可启动应用ID列表 - JSON字符串格式
     */
    @JsonProperty("ug_launchable_appid_list")
    private String ugLaunchableAppidList;

    /**
     * 音频主导色调
     */
    @JsonProperty("color_audio_dominate")
    private String colorAudioDominate;

    /**
     * 章节项目ID
     */
    @JsonProperty("item_id")
    private String itemId;

    /**
     * 书籍Flight别名缩略图
     */
    @JsonProperty("book_flight_alias_thumb")
    private String bookFlightAliasThumb;

    /**
     * 文章标题来源
     */
    @JsonProperty("title_from_article")
    private String titleFromArticle;

    /**
     * 原始应用级别
     */
    @JsonProperty("origin_app_level")
    private String originAppLevel;

    /**
     * 插入协议类型
     */
    @JsonProperty("inset_agreement_type")
    private String insetAgreementType;

    /**
     * 声誉缩略图URI
     */
    @JsonProperty("reputation_thumb_uri")
    private String reputationThumbUri;

    /**
     * 关注ID
     */
    @JsonProperty("concern_id")
    private String concernId;

    /**
     * 章节状态
     */
    @JsonProperty("item_status")
    private String itemStatus;

    /**
     * 点赞支持权限
     */
    @JsonProperty("praise_support_permission")
    private String praiseSupportPermission;

    /**
     * 副标题
     */
    @JsonProperty("sub_title")
    private String subTitle;

    /**
     * 版本信息
     */
    @JsonProperty("version")
    private String version;

    /**
     * 风险等级
     */
    @JsonProperty("risk_rate")
    private String riskRate;

    /**
     * 小说书籍缩略图类型
     */
    @JsonProperty("novel_book_thumb_type")
    private String novelBookThumbType;

    /**
     * 主要冷启动参数
     */
    @JsonProperty("major_coldstart")
    private String majorColdstart;

    /**
     * 书籍简称
     */
    @JsonProperty("book_short_name")
    private String bookShortName;

    /**
     * 番茄书籍状态
     */
    @JsonProperty("tomato_book_status")
    private String tomatoBookStatus;

    /**
     * 字数统计
     */
    @JsonProperty("word_number")
    private Integer wordNumber;

    /**
     * 真实章节顺序
     */
    @JsonProperty("real_chapter_order")
    private String realChapterOrder;

    /**
     * 将保持更新天数
     */
    @JsonProperty("will_keep_update_days")
    private String willKeepUpdateDays;

    /**
     * 平台书籍ID
     */
    @JsonProperty("platform_book_id")
    private String platformBookId;

    /**
     * 原始策略
     */
    @JsonProperty("origin_strategy")
    private String originStrategy;

    /**
     * 书籍名称
     */
    @JsonProperty("book_name")
    private String bookName;

    /**
     * 相关音频书籍ID
     */
    @JsonProperty("related_audio_book_id")
    private String relatedAudioBookId;

    /**
     * 书籍搜索可见性
     */
    @JsonProperty("book_search_visible")
    private String bookSearchVisible;

    /**
     * 视频ID信息 - JSON字符串格式
     * 包含不同音调对应的视频ID和时长信息
     */
    @JsonProperty("vid")
    private String vid;

    /**
     * 推荐数量级别
     */
    @JsonProperty("recommend_count_level")
    private String recommendCountLevel;

    /**
     * 人物角色设定
     */
    @JsonProperty("persona")
    private String persona;

    /**
     * 长度类型
     */
    @JsonProperty("length_type")
    private String lengthType;

    /**
     * 章节标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 移除类型
     */
    @JsonProperty("remove_type")
    private String removeType;

    /**
     * ISBN编号
     */
    @JsonProperty("isbn")
    private String isbn;

    /**
     * 声誉最新设置时间
     */
    @JsonProperty("reputation_latest_set_time")
    private String reputationLatestSetTime;

    /**
     * 类型标识
     */
    @JsonProperty("type")
    private String type;

    /**
     * 是否为内容章节
     */
    @JsonProperty("is_content")
    private String isContent;

    /**
     * 出版商
     */
    @JsonProperty("publisher")
    private String publisher;

    /**
     * 阅读礼物权限
     */
    @JsonProperty("reading_gift_permission")
    private String readingGiftPermission;

    /**
     * 内容MD5校验值
     */
    @JsonProperty("content_md5")
    private String contentMd5;

    /**
     * 子摘要
     */
    @JsonProperty("sub_abstract")
    private String subAbstract;

    /**
     * 音频时长
     */
    @JsonProperty("duration")
    private String duration;

    /**
     * 首次上线时间 - JSON字符串格式
     * 包含不同平台的上线时间戳
     */
    @JsonProperty("first_online_time")
    private String firstOnlineTime;

    /**
     * 章节缩略图URL
     */
    @JsonProperty("chapter_thumb_url")
    private String chapterThumbUrl;

    /**
     * 平台标识
     */
    @JsonProperty("platform")
    private String platform;

    /**
     * 折扣价格
     */
    @JsonProperty("discount_price")
    private String discountPrice;

    /**
     * 角色列表 - JSON字符串格式
     */
    @JsonProperty("roles")
    private String roles;

    /**
     * 所有权类型
     */
    @JsonProperty("ownership_type")
    private String ownershipType;

    /**
     * 首次通过的章节
     */
    @JsonProperty("first_pass_item")
    private String firstPassItem;

    /**
     * 分类V2信息 - JSON字符串格式
     * 包含详细的分类信息
     */
    @JsonProperty("category_v2")
    private String categoryV2;

    /**
     * 第三方合作伙伴URL
     */
    @JsonProperty("third_partern_url")
    private String thirdParternUrl;

    /**
     * 原始应用级别日期
     */
    @JsonProperty("origin_app_level_date")
    private String originAppLevelDate;

    /**
     * 原始作者信息 - JSON字符串格式
     */
    @JsonProperty("original_authors")
    private String originalAuthors;

    /**
     * 礼物权限
     */
    @JsonProperty("gift_permission")
    private String giftPermission;

    /**
     * 修改后的声誉书籍名称
     */
    @JsonProperty("modified_reputation_book_name")
    private String modifiedReputationBookName;

    /**
     * 声誉审核状态
     */
    @JsonProperty("reputation_audit_status")
    private String reputationAuditStatus;

    /**
     * 免费状态
     */
    @JsonProperty("free_status")
    private String freeStatus;

    /**
     * 音调时长信息 - JSON字符串格式
     * 包含不同音调的时长数据
     */
    @JsonProperty("tone_duration")
    private String toneDuration;

    /**
     * 最后发布时间
     */
    @JsonProperty("last_publish_time")
    private String lastPublishTime;

    /**
     * 书籍Flight版本ID
     */
    @JsonProperty("book_flight_version_id")
    private String bookFlightVersionId;

    /**
     * 章节顺序
     */
    @JsonProperty("order")
    private String order;

    /**
     * 章节类型
     */
    @JsonProperty("chapter_type")
    private String chapterType;

    /**
     * 自定义总价格
     */
    @JsonProperty("custom_total_price")
    private String customTotalPrice;

    /**
     * 销售类型
     */
    @JsonProperty("sale_type")
    private String saleType;

    /**
     * 是否为新书
     */
    @JsonProperty("is_new")
    private String isNew;

    /**
     * 内容章节数量
     */
    @JsonProperty("content_chapter_number")
    private String contentChapterNumber;

    /**
     * 相关书籍ID信息 - JSON字符串格式
     * 包含不同平台的相关书籍ID列表
     */
    @JsonProperty("related_bookids")
    private String relatedBookids;

    /**
     * 出版社
     */
    @JsonProperty("press")
    private String press;

    /**
     * 风险率延迟入队时间戳
     */
    @JsonProperty("risk_rate_delay_enqueue_ts")
    private String riskRateDelayEnqueueTs;

    /**
     * 首次通过时间
     */
    @JsonProperty("first_pass_time")
    private String firstPassTime;

    /**
     * 基础价格
     */
    @JsonProperty("base_price")
    private String basePrice;

    /**
     * 头部时间点时间戳
     */
    @JsonProperty("head_time_point_tms")
    private String headTimePointTms;

    /**
     * 状态码
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 独家标识
     */
    @JsonProperty("exclusive")
    private String exclusive;

    /**
     * 海报信息 - JSON字符串格式
     * 包含不同平台的海报配置
     */
    @JsonProperty("poster_info")
    private String posterInfo;

    /**
     * 类型
     */
    @JsonProperty("genre")
    private String genre;

    /**
     * 绑定声誉书籍ID
     */
    @JsonProperty("bind_reputation_book_id")
    private String bindReputationBookId;

    /**
     * 推荐审核员最后操作时间
     */
    @JsonProperty("recommend_auditor_last_time")
    private String recommendAuditorLastTime;

    /**
     * 论坛ID
     */
    @JsonProperty("forum_id")
    private String forumId;

    /**
     * 加入书架数量
     */
    @JsonProperty("add_bookshelf_count")
    private String addBookshelfCount;

    /**
     * 合同授权信息 - JSON字符串格式
     * 包含不同平台的TTS授权和分发配置
     */
    @JsonProperty("contract_authorize")
    private String contractAuthorize;

    /**
     * 保持发布天数
     */
    @JsonProperty("keep_publish_days")
    private String keepPublishDays;

    /**
     * 总价格
     */
    @JsonProperty("total_price")
    private String totalPrice;

    /**
     * 章节缩略图URI
     */
    @JsonProperty("chapter_thumb_uri")
    private String chapterThumbUri;

    /**
     * 数据评级
     */
    @JsonProperty("data_rate")
    private String dataRate;

    /**
     * 绑定作者ID
     */
    @JsonProperty("bind_author_ids")
    private String bindAuthorIds;

    /**
     * 摘要内容
     */
    @JsonProperty("abstract")
    private String abstractText;

    /**
     * 操作频道
     */
    @JsonProperty("op_channel")
    private String opChannel;

    /**
     * 阅读数量
     */
    @JsonProperty("read_count")
    private String readCount;

    /**
     * 折扣自定义总价格
     */
    @JsonProperty("discount_custom_total_price")
    private String discountCustomTotalPrice;

    /**
     * 章节音频缩略图URL
     */
    @JsonProperty("chapter_audio_thumb_url")
    private String chapterAudioThumbUrl;

    /**
     * 音频最受欢迎色调
     */
    @JsonProperty("color_audio_most_popular")
    private String colorAudioMostPopular;

    /**
     * 章节时长
     */
    @JsonProperty("item_duration")
    private String itemDuration;

    /**
     * 完整分类
     */
    @JsonProperty("complete_category")
    private String completeCategory;

    /**
     * 分类
     */
    @JsonProperty("category")
    private String category;

    /**
     * 授权类型
     */
    @JsonProperty("authorize_type")
    private String authorizeType;

    /**
     * 书籍Flight别名
     */
    @JsonProperty("book_flight_alias_name")
    private String bookFlightAliasName;

    /**
     * 章节字数
     */
    @JsonProperty("chapter_word_number")
    private Integer chapterWordNumber;

    /**
     * 长度档案分数
     */
    @JsonProperty("changdu_profile_score")
    private String changduProfileScore;

    /**
     * 组ID
     */
    @JsonProperty("group_id")
    private String groupId;

    /**
     * 相关音频书籍ID列表 - JSON字符串格式
     */
    @JsonProperty("related_audio_bookids")
    private String relatedAudioBookIds;

    /**
     * 前一个章节ID
     */
    @JsonProperty("pre_item_id")
    private String preItemId;

    /**
     * 是否需要付费
     */
    @JsonProperty("need_pay")
    private String needPay;

    /**
     * 预估章节总数
     */
    @JsonProperty("estimated_chapter_count")
    private String estimatedChapterCount;

    /**
     * 创作最新完成时间
     */
    @JsonProperty("creation_latest_finish_time")
    private String creationLatestFinishTime;

    /**
     * 从属媒体ID
     */
    @JsonProperty("slave_media_id")
    private String slaveMediaId;

    /**
     * 是否有额外章节
     */
    @JsonProperty("has_extra_chapter")
    private String hasExtraChapter;

    /**
     * 性别标识
     */
    @JsonProperty("gender")
    private String gender;

    /**
     * 真实独家标识
     */
    @JsonProperty("real_exclusive")
    private String realExclusive;

    /**
     * 作者修改章节开关
     */
    @JsonProperty("author_modify_chapter_switch")
    private String authorModifyChapterSwitch;

    /**
     * 标签
     */
    @JsonProperty("tags")
    private String tags;

    /**
     * 可见性信息 - JSON字符串格式
     * 包含平台可见性配置
     */
    @JsonProperty("visibility_info")
    private String visibilityInfo;

    /**
     * 书籍类型
     */
    @JsonProperty("book_type")
    private String bookType;

    /**
     * 推荐审核员状态
     */
    @JsonProperty("recommend_auditor_status")
    private String recommendAuditorStatus;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private String createTime;

    /**
     * 子类型
     */
    @JsonProperty("sub_genre")
    private String subGenre;

    /**
     * 发布日期
     */
    @JsonProperty("published_date")
    private String publishedDate;

    /**
     * 下一个组ID
     */
    @JsonProperty("next_group_id")
    private String nextGroupId;

    /**
     * 版权信息
     */
    @JsonProperty("copyright_info")
    private String copyrightInfo;

    /**
     * 媒体ID
     */
    @JsonProperty("media_id")
    private String mediaId;

    /**
     * 前一个组ID
     */
    @JsonProperty("pre_group_id")
    private String preGroupId;

    /**
     * 外部用户ID
     */
    @JsonProperty("external_uid")
    private String externalUid;

    /**
     * 缩略图确认状态
     */
    @JsonProperty("thumb_confirm_status")
    private String thumbConfirmStatus;

    /**
     * 广告免费显示
     */
    @JsonProperty("ad_free_show")
    private String adFreeShow;

    /**
     * 原始章节标题
     */
    @JsonProperty("origin_chapter_title")
    private String originChapterTitle;

    /**
     * 创作状态
     */
    @JsonProperty("creation_status")
    private String creationStatus;

    /**
     * 付费信息
     */
    @JsonProperty("pay_info")
    private String payInfo;

    /**
     * 平台章节ID
     */
    @JsonProperty("platform_chapter_id")
    private String platformChapterId;

    /**
     * 操作标签
     */
    @JsonProperty("op_tag")
    private String opTag;

    /**
     * 卷名
     */
    @JsonProperty("volume_name")
    private String volumeName;

    /**
     * 缩略图URL
     */
    @JsonProperty("thumb_url")
    private String thumbUrl;

    /**
     * 主导色调
     */
    @JsonProperty("color_dominate")
    private String colorDominate;

    /**
     * 是否为故事CP
     */
    @JsonProperty("is_story_cp")
    private String isStoryCp;

    /**
     * 更新停止状态
     */
    @JsonProperty("update_stop")
    private String updateStop;

    /**
     * 区域可见性信息 - JSON字符串格式
     * 包含不同地区的可见性和版权配置
     */
    @JsonProperty("region_visibility_info")
    private String regionVisibilityInfo;

    /**
     * VIP书籍标识
     */
    @JsonProperty("vip_book")
    private String vipBook;

    /**
     * Flight标志
     */
    @JsonProperty("flight_flag")
    private String flightFlag;

    /**
     * 原始书籍名称
     */
    @JsonProperty("original_book_name")
    private String originalBookName;

    /**
     * 收益时间
     */
    @JsonProperty("benefit_time")
    private String benefitTime;

    /**
     * 下一个章节ID
     */
    @JsonProperty("next_item_id")
    private String nextItemId;

    /**
     * 禁止城市
     */
    @JsonProperty("ban_city")
    private String banCity;

    /**
     * TTS状态
     */
    @JsonProperty("tts_status")
    private String ttsStatus;

    /**
     * 音调列表 - 逗号分隔的字符串
     */
    @JsonProperty("tones")
    private String tones;

    /**
     * 额外字数
     */
    @JsonProperty("extra_word_number")
    private String extraWordNumber;

    /**
     * 销售状态
     */
    @JsonProperty("sale_status")
    private String saleStatus;

    /**
     * 第三方阅读历史
     */
    @JsonProperty("third_read_history")
    private String thirdReadHistory;

    /**
     * 分类FM
     */
    @JsonProperty("category_fm")
    private String categoryFm;

    /**
     * 写作额外权限
     */
    @JsonProperty("write_extra_permission")
    private String writeExtraPermission;

    /**
     * 媒体书籍首次推荐时间
     */
    @JsonProperty("media_book_first_recommend_time")
    private String mediaBookFirstRecommendTime;

    /**
     * 作者名称
     */
    @JsonProperty("author")
    private String author;

    /**
     * 海报标志
     */
    @JsonProperty("poster_flag")
    private String posterFlag;

    /**
     * 保持更新天数
     */
    @JsonProperty("keep_update_days")
    private String keepUpdateDays;

    /**
     * 连载数量
     */
    @JsonProperty("serial_count")
    private String serialCount;

    /**
     * 签名申请时间
     */
    @JsonProperty("sign_apply_time")
    private String signApplyTime;

    /**
     * 礼物感谢词
     */
    @JsonProperty("gift_word")
    private String giftWord;

    /**
     * 首次可见时间 - JSON字符串格式
     * 包含不同平台的首次可见时间戳
     */
    @JsonProperty("first_visible_time")
    private String firstVisibleTime;

    /**
     * 分类V2 ID列表 - 逗号分隔
     */
    @JsonProperty("category_v2_ids")
    private String categoryV2Ids;

    /**
     * 缩略图URI
     */
    @JsonProperty("thumb_uri")
    private String thumbUri;

    /**
     * 最受欢迎色调
     */
    @JsonProperty("color_most_popular")
    private String colorMostPopular;

    /**
     * MP性别
     */
    @JsonProperty("mp_gender")
    private String mpGender;

    /**
     * 书籍ID
     */
    @JsonProperty("book_id")
    private String bookId;

    /**
     * 音频缩略图URI
     */
    @JsonProperty("audio_thumb_uri")
    private String audioThumbUri;

    /**
     * 来源
     */
    @JsonProperty("source")
    private String source;

    /**
     * TTS分发状态
     */
    @JsonProperty("tts_distribution")
    private String ttsDistribution;

    /**
     * TTS备份信息 - JSON字符串格式
     * 包含不同音调的备份视频ID和时长
     */
    @JsonProperty("tts_backup")
    private String ttsBackup;
}
