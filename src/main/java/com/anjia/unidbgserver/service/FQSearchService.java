package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.FQApiProperties;
import com.anjia.unidbgserver.dto.*;
import com.anjia.unidbgserver.utils.FQApiUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

/**
 * FQ书籍搜索和目录服务
 * 提供书籍搜索、目录获取等功能
 */
@Slf4j
@Service
public class FQSearchService {

    @Resource(name = "fqEncryptWorker")
    private FQEncryptServiceWorker fqEncryptServiceWorker;

    @Resource
    private FQApiProperties fqApiProperties;

    @Resource
    private FQApiUtils fqApiUtils;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 默认FQ变量配置
    private FqVariable defaultFqVariable;

    /**
     * 获取默认FQ变量（延迟初始化）
     */
    private FqVariable getDefaultFqVariable() {
        if (defaultFqVariable == null) {
            defaultFqVariable = new FqVariable(fqApiProperties);
        }
        return defaultFqVariable;
    }

    /**
     * 搜索书籍 - 增强版，支持两阶段搜索
     *
     * @param searchRequest 搜索请求参数
     * @return 搜索结果
     */
    public CompletableFuture<FQNovelResponse<FQSearchResponse>> searchBooksEnhanced(FQSearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 如果用户已经提供了search_id，直接进行搜索
                if (searchRequest.getSearchId() != null && !searchRequest.getSearchId().trim().isEmpty()) {
                    return performSearchWithId(searchRequest);
                }

                // 第一阶段：获取search_id
                FQSearchRequest firstRequest = createFirstPhaseRequest(searchRequest);
                FQNovelResponse<FQSearchResponse> firstResponse = performSearchInternal(firstRequest);

                if (firstResponse.getCode() != 0 || firstResponse.getData() == null ||
                    firstResponse.getData().getSearchId() == null) {
                    log.warn("第一阶段搜索失败或未返回search_id");
                    return firstResponse;
                }

                String searchId = firstResponse.getData().getSearchId();

                // 随机延迟 1-2 秒
                try {
                    long delay = 1000 + (long)(Math.random() * 1000); // 1000-2000ms
                    Thread.sleep(delay);
                    searchRequest.setLastSearchPageInterval((int) delay); // 设置间隔时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("延迟被中断", e);
                }

                // 第二阶段：使用search_id进行搜索
                FQSearchRequest secondRequest = createSecondPhaseRequest(searchRequest, searchId);
                FQNovelResponse<FQSearchResponse> secondResponse = performSearchInternal(secondRequest);

                // 确保返回结果包含search_id
                if (secondResponse.getCode() == 0 && secondResponse.getData() != null ){
                    secondResponse.getData().setSearchId(searchId);
                }

                return secondResponse;

            } catch (Exception e) {
                log.error("增强搜索失败 - query: {}", searchRequest.getQuery(), e);
                return FQNovelResponse.error("增强搜索失败: " + e.getMessage());
            }
        });
    }

    /**
     * 创建第一阶段请求（获取search_id）
     */
    private FQSearchRequest createFirstPhaseRequest(FQSearchRequest originalRequest) {
        FQSearchRequest firstRequest = new FQSearchRequest();

        // 复制所有基本参数
        copyBasicParameters(originalRequest, firstRequest);

        // 第一阶段特定设置
        firstRequest.setIsFirstEnterSearch(true);
        firstRequest.setClientAbInfo(originalRequest.getClientAbInfo()); // 包含client_ab_info
        firstRequest.setLastSearchPageInterval(0); // 第一次调用为0

        // 确保passback与offset相同
        if (firstRequest.getPassback() == null) {
            firstRequest.setPassback(firstRequest.getOffset());
        }

        return firstRequest;
    }

    /**
     * 创建第二阶段请求（使用search_id）
     */
    private FQSearchRequest createSecondPhaseRequest(FQSearchRequest originalRequest, String searchId) {
        FQSearchRequest secondRequest = new FQSearchRequest();

        // 复制所有基本参数
        copyBasicParameters(originalRequest, secondRequest);

        // 第二阶段特定设置
        secondRequest.setSearchId(searchId);
        secondRequest.setIsFirstEnterSearch(false); // 第二次调用设为false
        // 不设置client_ab_info（在buildSearchParams中会被跳过）

        // 确保passback与offset相同
        if (secondRequest.getPassback() == null) {
            secondRequest.setPassback(secondRequest.getOffset());
        }

        return secondRequest;
    }

    /**
     * 复制基本参数
     */
    private void copyBasicParameters(FQSearchRequest source, FQSearchRequest target) {
        target.setQuery(source.getQuery());
        target.setOffset(source.getOffset());
        target.setCount(source.getCount());
        target.setTabType(source.getTabType());
        target.setPassback(source.getPassback());
        target.setBookshelfSearchPlan(source.getBookshelfSearchPlan());
        target.setFromRs(source.getFromRs());
        target.setUserIsLogin(source.getUserIsLogin());
        target.setBookstoreTab(source.getBookstoreTab());
        target.setSearchSource(source.getSearchSource());
        target.setClickedContent(source.getClickedContent());
        target.setSearchSourceId(source.getSearchSourceId());
        target.setUseLynx(source.getUseLynx());
        target.setUseCorrect(source.getUseCorrect());
        target.setTabName(source.getTabName());
        target.setClientAbInfo(source.getClientAbInfo());
        target.setLineWordsNum(source.getLineWordsNum());
        target.setLastConsumeInterval(source.getLastConsumeInterval());
        target.setPadColumnCover(source.getPadColumnCover());
        target.setKlinkEgdi(source.getKlinkEgdi());
        target.setNormalSessionId(source.getNormalSessionId());
        target.setColdStartSessionId(source.getColdStartSessionId());
        target.setCharging(source.getCharging());
        target.setScreenBrightness(source.getScreenBrightness());
        target.setBatteryPct(source.getBatteryPct());
        target.setDownSpeed(source.getDownSpeed());
        target.setSysDarkMode(source.getSysDarkMode());
        target.setAppDarkMode(source.getAppDarkMode());
        target.setFontScale(source.getFontScale());
        target.setIsAndroidPadScreen(source.getIsAndroidPadScreen());
        target.setNetworkType(source.getNetworkType());
        target.setRomVersion(source.getRomVersion());
        target.setCurrentVolume(source.getCurrentVolume());
        target.setCdid(source.getCdid());
        target.setNeedPersonalRecommend(source.getNeedPersonalRecommend());
        target.setPlayerSoLoad(source.getPlayerSoLoad());
        target.setGender(source.getGender());
        target.setComplianceStatus(source.getComplianceStatus());
        target.setHarStatus(source.getHarStatus());
    }

    /**
     * 执行带search_id的搜索
     */
    private FQNovelResponse<FQSearchResponse> performSearchWithId(FQSearchRequest searchRequest) {
        // 确保is_first_enter_search为false，不包含client_ab_info
        searchRequest.setIsFirstEnterSearch(false);

        // 确保passback与offset相同
        if (searchRequest.getPassback() == null) {
            searchRequest.setPassback(searchRequest.getOffset());
        }

        return performSearchInternal(searchRequest);
    }

    /**
     * 执行实际的搜索请求
     */
    private FQNovelResponse<FQSearchResponse> performSearchInternal(FQSearchRequest searchRequest) {
        try {
            FqVariable var = getDefaultFqVariable();

            // 构建搜索URL和参数
            String url = fqApiUtils.getBaseUrl().replace("api5-normal-sinfonlineb", "api5-normal-sinfonlinec")
                + "/reading/bookapi/search/tab/v";
            Map<String, String> params = fqApiUtils.buildSearchParams(var, searchRequest);
            String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

            // 构建请求头
            Map<String, String> headers = fqApiUtils.buildCommonHeaders();
            headers.put("Authorization","Bearer");

            // 生成签名
            Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

            // 发起API请求
            HttpHeaders httpHeaders = new HttpHeaders();
            signedHeaders.forEach(httpHeaders::set);
            headers.forEach(httpHeaders::set);

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            URI uri = URI.create(fullUrl);

            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);

            // 解压缩 GZIP 响应体
            String responseBody = decompressGzipResponse(response.getBody());

            // 解析响应
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            int tabType = searchRequest.getTabType(); // 从请求获取需要的tab_type
            FQSearchResponse searchResponse = parseSearchResponse(jsonResponse, tabType);

            return FQNovelResponse.success(searchResponse);

        } catch (Exception e) {
            log.error("搜索请求失败 - query: {}", searchRequest.getQuery(), e);
            return FQNovelResponse.error("搜索请求失败: " + e.getMessage());
        }
    }

    /**
     * 搜索书籍
     *
     * @param searchRequest 搜索请求参数
     * @return 搜索结果
     */
    public CompletableFuture<FQNovelResponse<FQSearchResponse>> searchBooks(FQSearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FqVariable var = getDefaultFqVariable();

                // 构建搜索URL和参数
                String url = fqApiUtils.getBaseUrl().replace("api5-normal-sinfonlineb", "api5-normal-sinfonlinec")
                    + "/reading/bookapi/search/tab/v";
                Map<String, String> params = fqApiUtils.buildSearchParams(var, searchRequest);
                String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

                // 构建请求头
                Map<String, String> headers = fqApiUtils.buildCommonHeaders();

                headers.put("Authorization","Bearer");

                // 生成签名
                Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

                // 发起API请求
                HttpHeaders httpHeaders = new HttpHeaders();
                signedHeaders.forEach(httpHeaders::set);
                headers.forEach(httpHeaders::set);

                HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

                URI uri = URI.create(fullUrl);

                ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);

                // 解压缩 GZIP 响应体
                String responseBody = decompressGzipResponse(response.getBody());

                // 解析响应
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                int tabType = searchRequest.getTabType(); // 从请求获取需要的tab_type
                FQSearchResponse searchResponse = parseSearchResponse(jsonResponse,tabType);

                return FQNovelResponse.success(searchResponse);

            } catch (Exception e) {
                log.error("搜索书籍失败 - query: {}", searchRequest.getQuery(), e);
                return FQNovelResponse.error("搜索书籍失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取书籍目录
     *
     * @param directoryRequest 目录请求参数
     * @return 书籍目录
     */
    public CompletableFuture<FQNovelResponse<FQDirectoryResponse>> getBookDirectory(FQDirectoryRequest directoryRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FqVariable var = getDefaultFqVariable();

                // 构建目录URL和参数
                String url = fqApiUtils.getBaseUrl().replace("api5-normal-sinfonlineb", "api5-normal-sinfonlinec")
                    + "/reading/bookapi/directory/all_items/v";
                Map<String, String> params = fqApiUtils.buildDirectoryParams(var, directoryRequest);
                String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

                // 构建请求头
                Map<String, String> headers = fqApiUtils.buildCommonHeaders();

                // 生成签名
                Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

                // 发起API请求
                HttpHeaders httpHeaders = new HttpHeaders();
                signedHeaders.forEach(httpHeaders::set);
                headers.forEach(httpHeaders::set);

                HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
                ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, byte[].class);

                // 解压缩 GZIP 响应体
                String responseBody = decompressGzipResponse(response.getBody());

                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.get("data");

                FQDirectoryResponse directoryResponse = objectMapper.treeToValue(dataNode, FQDirectoryResponse.class);

                return FQNovelResponse.success(directoryResponse);

            } catch (Exception e) {
                log.error("获取书籍目录失败 - bookId: {}", directoryRequest.getBookId(), e);
                return FQNovelResponse.error("获取书籍目录失败: " + e.getMessage());
            }
        });
    }

    /**
     * 解压缩GZIP响应
     */
    private String decompressGzipResponse(byte[] gzipData) throws Exception {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(gzipData))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 解析搜索响应，根据 tabType 提取内容
     */
    public static FQSearchResponse parseSearchResponse(JsonNode jsonResponse, int tabType) {
        FQSearchResponse searchResponse = new FQSearchResponse();

        // search_tabs 是数组
        JsonNode searchTabs = jsonResponse.get("search_tabs");
        if (searchTabs != null && searchTabs.isArray()) {
            for (JsonNode tab : searchTabs) {
                if (tab.has("tab_type") && tab.get("tab_type").asInt() == tabType) {
                    List<FQSearchResponse.BookItem> books = new ArrayList<>();
                    JsonNode tabData = tab.get("data");
                    if (tabData != null && tabData.isArray()) {
                        for (JsonNode cell : tabData) {
                            JsonNode bookData = cell.get("book_data");
                            if (bookData != null && bookData.isArray()) {
                                for (JsonNode bookNode : bookData) {
                                    books.add(parseBookItem(bookNode));
                                }
                            }
                        }
                    }
                    searchResponse.setBooks(books);

                    // 解析 tab 的其他字段
                    searchResponse.setTotal(tab.path("total").asInt(books.size())); // 若没有 total 字段则用 books.size
                    searchResponse.setHasMore(tab.path("has_more").asBoolean(false));
                    searchResponse.setSearchId(tab.path("search_id").asText(""));

                    break;
                }
            }
        }
        return searchResponse;
    }

    /**
     * 解析书籍项目，字段映射按实际API返回
     */
    private static FQSearchResponse.BookItem parseBookItem(JsonNode bookNode) {
        FQSearchResponse.BookItem book = new FQSearchResponse.BookItem();

        book.setBookId(bookNode.path("book_id").asText(""));
        book.setBookName(bookNode.path("book_name").asText(""));
        book.setAuthor(bookNode.path("author").asText(""));
        book.setDescription(bookNode.path("abstract").asText(""));
        book.setCoverUrl(bookNode.path("thumb_url").asText(""));
        book.setCategory(bookNode.path("category").asText(""));

        // 标签兼容逗号分隔字符串和数组
        JsonNode tagsNode = bookNode.path("tags");
        if (tagsNode.isArray()) {
            List<String> tags = new ArrayList<>();
            for (JsonNode tag : tagsNode) {
                tags.add(tag.asText());
            }
            book.setTags(tags);
        } else {
            String tagsStr = tagsNode.asText("");
            if (!tagsStr.isEmpty()) {
                book.setTags(Arrays.asList(tagsStr.split(",")));
            }
        }
        book.setWordCount(bookNode.path("word_number").asLong(0));
        book.setStatus(bookNode.path("update_status").asText(""));
        book.setRating(bookNode.path("score").asDouble(0.0));
        book.setUpdateTime(bookNode.path("last_chapter_update_time").asLong(0));
        book.setLastChapterTitle(bookNode.path("last_chapter_title").asText(""));
        return book;
    }

    /**
     * 解析目录响应 - 基于实际API响应结构
     */
    private FQDirectoryResponse parseDirectoryResponse(JsonNode jsonResponse) {
        FQDirectoryResponse directoryResponse = new FQDirectoryResponse();

        if (jsonResponse.has("data")) {
            JsonNode dataNode = jsonResponse.get("data");
            // 解析附加数据列表
            if (dataNode.has("additional_item_data_list")) {
                directoryResponse.setAdditionalItemDataList(dataNode.get("additional_item_data_list"));
            }

            // 解析目录数据
            if (dataNode.has("catalog_data") && dataNode.get("catalog_data").isArray()) {
                List<FQDirectoryResponse.CatalogItem> catalogItems = new ArrayList<>();
                for (JsonNode catalogNode : dataNode.get("catalog_data")) {
                    FQDirectoryResponse.CatalogItem catalogItem = parseCatalogItem(catalogNode);
                    catalogItems.add(catalogItem);
                }
                directoryResponse.setCatalogData(catalogItems);
            }

            // 解析章节详细数据列表
            if (dataNode.has("item_data_list") && dataNode.get("item_data_list").isArray()) {
                List<FQDirectoryResponse.ItemData> itemDataList = new ArrayList<>();
                for (JsonNode itemNode : dataNode.get("item_data_list")) {
                    FQDirectoryResponse.ItemData itemData = parseItemData(itemNode);
                    itemDataList.add(itemData);
                }
                directoryResponse.setItemDataList(itemDataList);
            }

            // 解析字段缓存状态
            if (dataNode.has("field_cache_status")) {
                JsonNode cacheStatusNode = dataNode.get("field_cache_status");
                FQDirectoryResponse.FieldCacheStatus cacheStatus = parseCacheStatus(cacheStatusNode);
                directoryResponse.setFieldCacheStatus(cacheStatus);
            }

        }

        return directoryResponse;
    }

    /**
     * 解析目录项目
     */
    private FQDirectoryResponse.CatalogItem parseCatalogItem(JsonNode catalogNode) {
        FQDirectoryResponse.CatalogItem catalogItem = new FQDirectoryResponse.CatalogItem();

        catalogItem.setCatalogId(catalogNode.path("catalog_id").asText(""));
        catalogItem.setCatalogTitle(catalogNode.path("catalog_title").asText(""));
        catalogItem.setItemId(catalogNode.path("item_id").asText(""));

        return catalogItem;
    }

    /**
     * 解析章节详细数据
     */
    private FQDirectoryResponse.ItemData parseItemData(JsonNode itemNode) {
        FQDirectoryResponse.ItemData itemData = new FQDirectoryResponse.ItemData();

        itemData.setItemId(itemNode.path("item_id").asText(""));
        itemData.setVersion(itemNode.path("version").asText(""));
        itemData.setContentMd5(itemNode.path("content_md5").asText(""));
        itemData.setFirstPassTime(itemNode.path("first_pass_time").asInt(0));
        itemData.setTitle(itemNode.path("title").asText(""));
        itemData.setVolumeName(itemNode.path("volume_name").asText(""));
        itemData.setChapterType(itemNode.path("chapter_type").asText(""));
        itemData.setChapterWordNumber(itemNode.path("chapter_word_number").asInt(0));
        itemData.setIsReview(itemNode.path("is_review").asBoolean(false));

        return itemData;
    }

    /**
     * 解析缓存状态
     */
    private FQDirectoryResponse.FieldCacheStatus parseCacheStatus(JsonNode cacheStatusNode) {
        FQDirectoryResponse.FieldCacheStatus cacheStatus = new FQDirectoryResponse.FieldCacheStatus();

        if (cacheStatusNode.has("book_info")) {
            JsonNode bookInfoCacheNode = cacheStatusNode.get("book_info");
            FQDirectoryResponse.CacheInfo bookInfoCache = new FQDirectoryResponse.CacheInfo();
            bookInfoCache.setHit(bookInfoCacheNode.path("hit").asBoolean(false));
            bookInfoCache.setMd5(bookInfoCacheNode.path("md5").asText(""));
            cacheStatus.setBookInfo(bookInfoCache);
        }

        if (cacheStatusNode.has("item_data_list")) {
            JsonNode itemDataCacheNode = cacheStatusNode.get("item_data_list");
            FQDirectoryResponse.CacheInfo itemDataCache = new FQDirectoryResponse.CacheInfo();
            itemDataCache.setHit(itemDataCacheNode.path("hit").asBoolean(false));
            itemDataCache.setMd5(itemDataCacheNode.path("md5").asText(""));
            cacheStatus.setItemDataList(itemDataCache);
        }

        return cacheStatus;
    }
}
