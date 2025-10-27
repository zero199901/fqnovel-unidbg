package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

/**
 * FQNovel 小说书籍信息（完整版）
 */
@Data
public class FQNovelBookInfo {

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
     * 作者详细信息
     */
    private Map<String, Object> authorInfo;

    /**
     * 书籍描述
     */
    private String description;

    /**
     * 书籍摘要V2
     */
    private String bookAbstractV2;

    /**
     * 书籍封面URL
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
     * 书籍状态 (0: 连载中, 1: 已完结)
     */
    private Integer status;

    /**
     * 创作状态
     */
    private String creationStatus;

    /**
     * 更新状态
     */
    private String updateStatus;

    /**
     * 更新停止标志
     */
    private String updateStop;

    // ============ 章节信息 ============

    /**
     * 章节总数
     */
    private Integer totalChapters;

    /**
     * 字数
     */
    private String wordNumber;

    /**
     * 第一章标题
     */
    private String firstChapterTitle;

    /**
     * 第一章ID
     */
    private String firstChapterItemId;

    /**
     * 第一章组ID
     */
    private String firstChapterGroupId;

    /**
     * 最新章节标题
     */
    private String lastChapterTitle;

    /**
     * 最后章节ID
     */
    private String lastChapterItemId;

    /**
     * 最后章节组ID
     */
    private String lastChapterGroupId;

    /**
     * 最后章节更新时间
     */
    private String lastChapterUpdateTime;

    /**
     * 最后章节首次通过时间
     */
    private String lastChapterFirstPassTime;

    /**
     * 真实章节顺序
     */
    private String realChapterOrder;

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
     * 分类V2 ID列表
     */
    private String categoryV2Ids;

    /**
     * 分类模式
     */
    private String categorySchema;

    /**
     * 完整分类
     */
    private String completeCategory;

    /**
     * 纯分类标签
     */
    private String pureCategoryTags;

    /**
     * 类型
     */
    private String genre;

    /**
     * 类型类别
     */
    private String genreType;

    /**
     * 子类型
     */
    private String subGenre;

    /**
     * 标签
     */
    private String tags;

    /**
     * 性别
     */
    private String gender;

    // ============ 统计数据 ============

    /**
     * 阅读数量
     */
    private String readCount;

    /**
     * 所有阅读数
     */
    private String readCountAll;

    /**
     * 阅读数量文本
     */
    private String readCntText;

    /**
     * 30天阅读数量
     */
    private String readDcnt30d;

    /**
     * 加入书架数量
     */
    private String addBookshelfCount;

    /**
     * 所有书架数量
     */
    private String allBookshelfCount;

    /**
     * 14天加入书架数量
     */
    private String addShelfCount14d;

    /**
     * 书架数量历史
     */
    private String shelfCntHistory;

    /**
     * 14天读者UV
     */
    private String readerUv14day;

    /**
     * 每日读者UV总和
     */
    private String readerUvSumDaily;

    /**
     * 收听数量
     */
    private String listenCount;

    /**
     * 14天收听UV
     */
    private String listenUv14day;

    /**
     * 30天收听UV
     */
    private String listenUv30day;

    /**
     * 评分
     */
    private String score;

    /**
     * 10%完成率
     */
    private String finishRate10;

    /**
     * 数据评级
     */
    private String dataRate;

    /**
     * 风险率
     */
    private String riskRate;

    /**
     * 推荐数量级别
     */
    private String recommendCountLevel;

    // ============ 价格与销售 ============

    /**
     * 总价格
     */
    private String totalPrice;

    /**
     * 自定义总价格
     */
    private String customTotalPrice;

    /**
     * 折扣价格
     */
    private String discountPrice;

    /**
     * 折扣自定义总价格
     */
    private String discountCustomTotalPrice;

    /**
     * 基础价格
     */
    private String basePrice;

    /**
     * 销售状态
     */
    private String saleStatus;

    /**
     * 销售类型
     */
    private String saleType;

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
     * 授权类型
     */
    private String authorizeType;

    /**
     * 版权信息
     */
    private String copyrightInfo;

    /**
     * 合同授权
     */
    private String contractAuthorize;

    // ============ 音频相关 ============

    /**
     * 音频缩略图URI
     */
    private String audioThumbUri;

    /**
     * 高清音频缩略图URL
     */
    private String audioThumbUrlHd;

    /**
     * 音频主导色
     */
    private String colorAudioDominate;

    /**
     * 音频主色调
     */
    private String colorAudioMostPopular;

    /**
     * 音频启用随机播放
     */
    private Boolean audioEnableRandomPlay;

    /**
     * 隐藏收听球
     */
    private Boolean hideListenBall;

    /**
     * 时长
     */
    private String duration;

    /**
     * 相关音频书籍ID
     */
    private String relatedAudioBookId;

    /**
     * 相关音频书籍ID列表
     */
    private String relatedAudioBookids;

    /**
     * 是否有匹配的音频书籍
     */
    private String hasMatchAudioBooks;

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

    /**
     * 使用方形图片
     */
    private String useSquarePic;

    /**
     * 缩略图确认状态
     */
    private String thumbConfirmStatus;

    /**
     * 操作缩略图URI
     */
    private String opThumbUri;

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

    /**
     * 首次可见时间
     */
    private String firstVisibleTime;

    /**
     * 最新阅读时间
     */
    private String latestReadTime;

    /**
     * 最新收听时间
     */
    private String latestListenTime;

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
     * 是否为老白
     */
    private String isLaobai;

    /**
     * 长度类型
     */
    private String lengthType;

    /**
     * 小说文本类型
     */
    private String novelTextType;

    /**
     * 小说书籍缩略图类型
     */
    private String novelBookThumbType;

    // ============ 其他信息 ============

    /**
     * 书籍搜索可见性
     */
    private String bookSearchVisible;

    /**
     * 可见性信息
     */
    private String visibilityInfo;

    /**
     * 区域可见性信息
     */
    private String regionVisibilityInfo;

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
     * 平台书籍ID
     */
    private String platformBookId;

    /**
     * Flight标志
     */
    private String flightFlag;

    /**
     * 书籍flight版本ID
     */
    private String bookFlightVersionId;

    /**
     * 书籍flight别名名称
     */
    private String bookFlightAliasName;

    /**
     * 书籍flight别名缩略图
     */
    private String bookFlightAliasThumb;

    /**
     * 绑定声誉书籍ID
     */
    private String bindReputationBookId;

    /**
     * 修改声誉书籍名称
     */
    private String modifiedReputationBookName;

    /**
     * 声誉缩略图URI
     */
    private String reputationThumbUri;

    /**
     * 声誉审核状态
     */
    private String reputationAuditStatus;

    /**
     * 声誉最新设置时间
     */
    private String reputationLatestSetTime;

    /**
     * 额外字数
     */
    private String extraWordNumber;

    /**
     * 是否有额外章节
     */
    private String hasExtraChapter;

    /**
     * 作者修改章节开关
     */
    private String authorModifyChapterSwitch;

    /**
     * 绑定作者ID列表
     */
    private String bindAuthorIds;

    /**
     * 保持发布天数
     */
    private String keepPublishDays;

    /**
     * 保持更新天数
     */
    private String keepUpdateDays;

    /**
     * 将保持更新天数
     */
    private String willKeepUpdateDays;

    /**
     * 预估章节数
     */
    private String estimatedChapterCount;

    /**
     * 内容章节数
     */
    private String contentChapterNumber;

    /**
     * 禁用阅读器功能
     */
    private Integer disableReaderFeature;

    /**
     * TTS状态
     */
    private String ttsStatus;

    /**
     * TTS分发状态
     */
    private String ttsDistribution;

    /**
     * TTS推荐屏蔽
     */
    private String ttsRecBlock;

    /**
     * 长度分数
     */
    private String changduProfileScore;

    /**
     * 写作额外权限
     */
    private String writeExtraPermission;

    /**
     * 创作最新完成时间
     */
    private String creationLatestFinishTime;
}
