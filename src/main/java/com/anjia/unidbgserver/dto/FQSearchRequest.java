package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQ搜索请求DTO
 * 补全所有实际API参数，保证与真实接口参数一致
 */
@Data
public class FQSearchRequest {

    // 搜索关键词
    private String query;

    // 分页偏移量
    private Integer offset = 0;

    // 每页数量
    private Integer count = 20;

    // passback参数 (与offset数值相同)
    private Integer passback;

    // 搜索类型 1综合 2听书 3书籍 4社区 5全文 6用户 8漫画 11短剧 13买书
    private Integer tabType;

    // 搜索ID (用于二次搜索)
    private String searchId;

    // 书架搜索计划
    private Integer bookshelfSearchPlan = 4;

    // 是否来自推荐系统
    private Boolean fromRs = false;

    // 用户是否登录
    private Integer userIsLogin = 0;

    // 书店标签
    private Integer bookstoreTab = 2;

    // 搜索来源
    private Integer searchSource = 1;

    // 点击内容类型
    private String clickedContent = "search_history";

    // 搜索来源ID
    private String searchSourceId = "his###";

    // 是否使用lynx
    private Boolean useLynx = false;

    // 是否使用纠错
    private Boolean useCorrect = true;

    // 标签页名称
    private String tabName = "store";

    // 是否首次进入搜索
    private Boolean isFirstEnterSearch = true;

    // 客户端AB测试信息（JSON字符串，需URL编码）
    private String clientAbInfo = "{}";

    // 新增以下字段，补全所有抓包参数，建议从设备/环境/session动态生成

    // 搜索页面停留时长
    private Integer lastSearchPageInterval = 0;

    // 每行字数
    private Integer lineWordsNum = 0;

    // 最近消费间隔
    private Integer lastConsumeInterval = 0;

    // pad显示封面
    private Integer padColumnCover = 0;

    // 用于设备唯一标识
    private String klinkEgdi = "";

    // 会话相关
    private String normalSessionId = "f9dd67fc-6150-4f44-8b92-aee2babf74e5";
    private String coldStartSessionId = "2b7d66c2-9fc6-4c50-bc85-d0f60f1d3c6e";

    // 充电状态
    private Integer charging = 1;

    // 屏幕亮度
    private Integer screenBrightness = 72;

    // 电池百分比
    private Integer batteryPct = 78;

    // 下载速度
    private Integer downSpeed = 89121;

    // 系统暗色模式
    private Integer sysDarkMode = 0;

    // app暗色模式
    private Integer appDarkMode = 0;

    // 字体缩放
    private Integer fontScale = 100;

    // 是否Pad屏幕
    private Integer isAndroidPadScreen = 0;

    // 网络类型
    private Integer networkType = 4;

    // ROM版本
    private String romVersion = "V417IR release-keys";

    // 当前音量
    private Integer currentVolume = 75;

    // 设备唯一标识
    private String cdid = "17f05006-423a-4172-be4b-7d26a42f2f4a";

    // 是否需要个性化推荐
    private Integer needPersonalRecommend = 0;

    // 播放器so加载
    private Integer playerSoLoad = 1;

    // 性别
    private Integer gender = 1;

    // 合规状态
    private Integer complianceStatus = 0;

    // HAR状态
    private Integer harStatus = 0;
}
