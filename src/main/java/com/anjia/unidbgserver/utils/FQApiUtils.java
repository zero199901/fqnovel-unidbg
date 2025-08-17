package com.anjia.unidbgserver.utils;

import com.anjia.unidbgserver.config.FQApiProperties;
import com.anjia.unidbgserver.dto.FQSearchRequest;
import com.anjia.unidbgserver.dto.FqVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * FQ API 通用工具类
 * 用于构建API请求参数和请求头
 */
@Component
@RequiredArgsConstructor
public class FQApiUtils {

    private final FQApiProperties fqApiProperties;

    /**
     * 构建通用API请求参数
     * 从FqVariable提取通用参数并构建Map
     *
     * @param var FQ变量配置
     * @return 参数映射
     */
    public Map<String, String> buildCommonApiParams(FqVariable var) {
        Map<String, String> params = new HashMap<>();

        // 基础设备参数
        params.put("iid", var.getInstallId());
        params.put("device_id", var.getDeviceId());
        params.put("ac", var.getAc());
        params.put("channel", var.getChannel());
        params.put("aid", var.getAid());
        params.put("app_name", var.getAppName());
        params.put("version_code", var.getVersionCode());
        params.put("version_name", var.getVersionName());
        params.put("device_platform", var.getDevicePlatform());
        params.put("os", var.getOs());
        params.put("ssmix", var.getSsmix());
        params.put("device_type", var.getDeviceType());
        params.put("device_brand", var.getDeviceBrand());
        params.put("language", var.getLanguage());
        params.put("os_api", var.getOsApi());
        params.put("os_version", var.getOsVersion());
        params.put("manifest_version_code", var.getManifestVersionCode());
        params.put("resolution", var.getResolution());
        params.put("dpi", var.getDpi());
        params.put("update_version_code", var.getUpdateVersionCode());
//        params.put("_rticket", var.getRticket());
        params.put("_rticket", String.valueOf(System.currentTimeMillis())); // 使用当前时间戳作为_rticket
        params.put("host_abi", var.getHostAbi());
        params.put("dragon_device_type", var.getDragonDeviceType());
        params.put("pv_player", var.getPvPlayer());
        params.put("compliance_status", var.getComplianceStatus());
        params.put("need_personal_recommend", var.getNeedPersonalRecommend());
        params.put("player_so_load", var.getPlayerSoLoad());
        params.put("is_android_pad_screen", var.getIsAndroidPadScreen());
        params.put("rom_version", var.getRomVersion());
        params.put("cdid", var.getCdid());

        return params;
    }

    /**
     * 构建batchFull特定的API参数
     * 在通用参数基础上添加batchFull特定参数
     *
     * @param var FQ变量配置
     * @param itemIds 章节ID列表
     * @param bookId 书籍ID
     * @param download 是否下载模式
     * @return 参数映射
     */
    public Map<String, String> buildBatchFullParams(FqVariable var, String itemIds, String bookId, boolean download) {
        Map<String, String> params = buildCommonApiParams(var);

        // batchFull特定参数
        params.put("item_ids", itemIds);
        params.put("key_register_ts", var.getKeyRegisterTs());
        params.put("book_id", bookId != null ? bookId : "7276384138653862966");
        params.put("req_type", download ? "0" : "1");

        return params;
    }

    /**
     * 构建通用请求头
     * 创建包含认证信息和标准头部的请求头映射
     *
     * @return 请求头映射
     */
    public Map<String, String> buildCommonHeaders() {
        return buildCommonHeaders(System.currentTimeMillis());
    }

    /**
     * 构建通用请求头（指定时间戳）
     * 创建包含认证信息和标准头部的请求头映射
     *
     * @param currentTime 当前时间戳
     * @return 请求头映射
     */
    public Map<String, String> buildCommonHeaders(long currentTime) {
        Map<String, String> headers = new HashMap<>();

        // 从配置获取Cookie和User-Agent
        headers.put("Cookie", fqApiProperties.getCookie());
        headers.put("User-Agent", fqApiProperties.getUserAgent());

        // 标准请求头
        headers.put("Accept", "application/json; charset=utf-8,application/x-protobuf");
        headers.put("Accept-Encoding", "gzip");
        headers.put("x-xs-from-web", "0");
        headers.put("x-ss-req-ticket", String.valueOf(currentTime));
        headers.put("x-reading-request", currentTime + "-" + (int)(Math.random() * 2000000000));
        headers.put("x-vc-bdturing-sdk-version", "3.7.2.cn");
        headers.put("lc", "101");
        headers.put("sdk-version", "2");
        headers.put("passport-sdk-version", "50564");
        headers.put("x-tt-store-region", "cn-zj");
        headers.put("x-tt-store-region-src", "did");

        return headers;
    }

    /**
     * 构建RegisterKey请求头
     * 在通用请求头基础上添加RegisterKey特定头部
     *
     * @return 请求头映射
     */
    public Map<String, String> buildRegisterKeyHeaders() {
        return buildRegisterKeyHeaders(System.currentTimeMillis());
    }

    /**
     * 构建RegisterKey请求头（指定时间戳）
     * 在通用请求头基础上添加RegisterKey特定头部
     *
     * @param currentTime 当前时间戳
     * @return 请求头映射
     */
    public Map<String, String> buildRegisterKeyHeaders(long currentTime) {
        Map<String, String> headers = buildCommonHeaders(currentTime);

        // RegisterKey特定头部
        headers.put("Content-Type", "application/json");

        return headers;
    }

    /**
     * 构建带参数的URL
     * 只对白名单参数进行编码，其他参数直接拼接，避免二次编码问题
     *
     * @param baseUrl 基础URL
     * @param params 参数映射
     * @return 完整URL
     */
    private static final Set<String> ENCODE_WHITELIST = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "query", "client_ab_info", "search_source_id", "search_id","device_type","resolution"
    )));

    public String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            String key = entry.getKey();
            String value = entry.getValue();
            urlBuilder.append(key).append("=");

            if (ENCODE_WHITELIST.contains(key)) {
                urlBuilder.append(encodeIfNeeded(value));
            } else {
                urlBuilder.append(value != null ? value : "");
            }
            first = false;
        }

        return urlBuilder.toString();
    }

    /**
     * 对参数值进行编码（已编码过的不再编码）
     */
    private String encodeIfNeeded(String value) {
        if (value == null) return "";
        // 如果已包含%，认为已编码过，不再编码
        if (value.contains("%")) {
            return value;
        }
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

public Map<String, String> buildSearchParams(FqVariable var, FQSearchRequest searchRequest) {
    Map<String, String> params = buildCommonApiParams(var);

    // 搜索特定参数，全部补齐，部分可动态生成
    params.put("bookshelf_search_plan", String.valueOf(searchRequest.getBookshelfSearchPlan()));
    params.put("offset", String.valueOf(searchRequest.getOffset()));
    params.put("from_rs", String.valueOf(searchRequest.getFromRs()));
    params.put("user_is_login", String.valueOf(searchRequest.getUserIsLogin()));
    params.put("bookstore_tab", String.valueOf(searchRequest.getBookstoreTab()));
    params.put("query", searchRequest.getQuery()); // 中文需编码，建议提前处理
    params.put("count", String.valueOf(searchRequest.getCount()));
    params.put("search_source", String.valueOf(searchRequest.getSearchSource()));
    params.put("clicked_content", searchRequest.getClickedContent());
    params.put("search_source_id", searchRequest.getSearchSourceId());
    params.put("use_lynx", String.valueOf(searchRequest.getUseLynx()));
    params.put("use_correct", String.valueOf(searchRequest.getUseCorrect()));
    params.put("last_search_page_interval", String.valueOf(searchRequest.getLastSearchPageInterval()));
    params.put("line_words_num", String.valueOf(searchRequest.getLineWordsNum()));
    params.put("tab_name", searchRequest.getTabName());
    params.put("last_consume_interval", String.valueOf(searchRequest.getLastConsumeInterval()));
    params.put("pad_column_cover", String.valueOf(searchRequest.getPadColumnCover()));
    params.put("is_first_enter_search", String.valueOf(searchRequest.getIsFirstEnterSearch()));

    // 添加search_id参数（如果存在）
    if (searchRequest.getSearchId() != null && !searchRequest.getSearchId().trim().isEmpty()) {
        params.put("search_id", searchRequest.getSearchId());
    }

    // 添加passback参数（与offset相同）
    if (searchRequest.getPassback() != null) {
        params.put("passback", String.valueOf(searchRequest.getPassback()));
    } else {
        // 如果没有设置passback，使用offset的值
        params.put("passback", String.valueOf(searchRequest.getOffset()));
    }

    // 添加tab_type参数
    if (searchRequest.getTabType() != null) {
        params.put("tab_type", String.valueOf(searchRequest.getTabType()));
    }

    // 只在is_first_enter_search为true时添加client_ab_info
    if (searchRequest.getIsFirstEnterSearch()) {
        params.put("client_ab_info", searchRequest.getClientAbInfo()); // JSON需编码
    }

//    params.put("klink_egdi", var.getKlinkEgdi()); // 设备特有参数
    params.put("normal_session_id", searchRequest.getNormalSessionId());
    params.put("cold_start_session_id", searchRequest.getColdStartSessionId());
    params.put("charging", String.valueOf(searchRequest.getCharging()));
    params.put("screen_brightness", String.valueOf(searchRequest.getScreenBrightness()));
    params.put("battery_pct", String.valueOf(searchRequest.getBatteryPct()));
    params.put("down_speed", String.valueOf(searchRequest.getDownSpeed()));
    params.put("sys_dark_mode", String.valueOf(searchRequest.getSysDarkMode()));
    params.put("app_dark_mode", String.valueOf(searchRequest.getAppDarkMode()));
    params.put("font_scale", String.valueOf(searchRequest.getFontScale()));
    params.put("is_android_pad_screen", String.valueOf(searchRequest.getIsAndroidPadScreen()));
    params.put("network_type", String.valueOf(searchRequest.getNetworkType()));
    params.put("current_volume", String.valueOf(searchRequest.getCurrentVolume()));
    // ... 补齐所有参数

    return params;
}

    /**
     * 构建目录API参数
     * 在通用参数基础上添加目录特定参数
     *
     * @param var FQ变量配置
     * @param directoryRequest 目录请求参数
     * @return 参数映射
     */
    public Map<String, String> buildDirectoryParams(FqVariable var, com.anjia.unidbgserver.dto.FQDirectoryRequest directoryRequest) {
        Map<String, String> params = buildCommonApiParams(var);

        // 目录特定参数
        params.put("book_type", String.valueOf(directoryRequest.getBookType()));
        params.put("book_id", directoryRequest.getBookId());
        params.put("need_version", String.valueOf(directoryRequest.getNeedVersion()));

        // 可选MD5参数
        if (directoryRequest.getItemDataListMd5() != null) {
            params.put("item_data_list_md5", directoryRequest.getItemDataListMd5());
        }
        if (directoryRequest.getCatalogDataMd5() != null) {
            params.put("catalog_data_md5", directoryRequest.getCatalogDataMd5());
        }
        if (directoryRequest.getBookInfoMd5() != null) {
            params.put("book_info_md5", directoryRequest.getBookInfoMd5());
        }

        return params;
    }

    /**
     * 获取API基础URL
     *
     * @return API基础URL
     */
    public String getBaseUrl() {
        return fqApiProperties.getBaseUrl();
    }
}
