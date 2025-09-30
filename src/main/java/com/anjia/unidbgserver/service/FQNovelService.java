package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.FQApiProperties;
import com.anjia.unidbgserver.dto.*;
import com.anjia.unidbgserver.service.FqCrypto;
import com.anjia.unidbgserver.utils.FQApiUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * FQNovel 小说内容获取服务
 * 基于 fqnovel-api 的 Rust 实现移植
 */
@Slf4j
@Service
public class FQNovelService {

    @Resource(name = "fqEncryptWorker")
    private FQEncryptServiceWorker fqEncryptServiceWorker;

    @Resource
    private FQRegisterKeyService registerKeyService;

    @Resource
    private FQApiProperties fqApiProperties;

    @Resource
    private FQApiUtils fqApiUtils;

    @Resource
    private FQSearchService fqSearchService;

    @Resource
    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private DeviceRotationService deviceRotationService;

    // 默认FQ变量配置（保留向后兼容）
    private FqVariable defaultFqVariable;

    /**
     * 获取FQ变量（支持设备轮换）
     */
    private FqVariable getFqVariable() {
        return deviceRotationService.getCurrentDevice();
    }

    /**
     * 获取默认FQ变量（延迟初始化，向后兼容）
     */
    private FqVariable getDefaultFqVariable() {
        if (defaultFqVariable == null) {
            defaultFqVariable = new FqVariable(fqApiProperties);
        }
        return defaultFqVariable;
    }

    /**
     * 批量获取章节内容 (基于 fqnovel-api 的 batch_full 方法)
     *
     * @param itemIds 章节ID列表，逗号分隔
     * @param bookId 书籍ID
     * @param download 是否下载模式 (false=在线阅读, true=下载)
     * @return 批量内容响应
     */
    public CompletableFuture<FQNovelResponse<FqIBatchFullResponse>> batchFull(String itemIds, String bookId, boolean download) {
        return batchFullWithRaw(itemIds, bookId, download, false).thenApply(response -> {
            if (response.getCode() == 0) {
                return FQNovelResponse.success(response.getData().getBatchResponse());
            } else {
                return FQNovelResponse.error(response.getMessage());
            }
        });
    }

    /**
     * 批量获取章节内容 (支持返回原始响应)
     *
     * @param itemIds 章节ID列表，逗号分隔
     * @param bookId 书籍ID
     * @param download 是否下载模式 (false=在线阅读, true=下载)
     * @param includeRawResponse 是否包含原始响应信息
     * @return 批量内容响应（包含原始响应信息）
     */
    public CompletableFuture<FQNovelResponse<BatchFullResponseWithRaw>> batchFullWithRaw(String itemIds, String bookId, boolean download, boolean includeRawResponse) {
        return CompletableFuture.supplyAsync(() -> {
            int maxAttempts = 3;
            int deviceSwitchThreshold = 2; // 失败2次后切换设备
            int consecutiveFailures = 0;
            
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                try {
                    // 如果连续失败超过阈值，尝试切换设备
                    if (consecutiveFailures >= deviceSwitchThreshold && attempt > 0) {
                        log.warn("连续失败{}次，尝试切换设备...", consecutiveFailures);
                        try {
                            deviceRotationService.switchToNextDevice();
                            log.info("已切换到下一个设备");
                            consecutiveFailures = 0; // 重置失败计数
                        } catch (Exception e) {
                            log.error("切换设备失败", e);
                        }
                    }
                    
                    FqVariable var = getFqVariable();

                    // 使用工具类构建URL和参数
                    String url = fqApiUtils.getBaseUrl() + "/reading/reader/batch_full/v";
                    Map<String, String> params = fqApiUtils.buildBatchFullParams(var, itemIds, bookId, download);
                    String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

                    log.info("批量获取章节内容 - 第{}次尝试, URL: {}, itemIds: {}, bookId: {}", 
                        attempt + 1, fullUrl, itemIds, bookId);

                    // 使用工具类构建请求头
                    Map<String, String> headers = fqApiUtils.buildCommonHeaders();

                    // 使用现有的签名服务生成签名
                    Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

                    // 发起API请求
                    HttpHeaders httpHeaders = new HttpHeaders();
                    signedHeaders.forEach(httpHeaders::set);
                    headers.forEach(httpHeaders::set);

                    HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
                    
                    // 记录请求时间戳
                    long requestTimestamp = System.currentTimeMillis();
                    ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, byte[].class);
                    long responseTimestamp = System.currentTimeMillis();

                    // 记录响应状态信息
                    log.info("API响应状态: {}, 响应头: {}", response.getStatusCode(), response.getHeaders());

                    // 处理响应体 - 支持GZIP和普通JSON格式
                    String responseBody = "";
                    byte[] responseBytes = response.getBody();
                    
                    if (responseBytes == null || responseBytes.length == 0) {
                        log.error("响应体为空 - 状态码: {}, 响应头: {}", response.getStatusCode(), response.getHeaders());
                        if (attempt < maxAttempts - 1) {
                            log.warn("响应体为空，准备重试 - 第{}次", attempt + 1);
                            try {
                                Thread.sleep(1000 * (attempt + 1)); // 递增延迟
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                        return FQNovelResponse.error("API响应为空，状态码: " + response.getStatusCode());
                    }
                    
                    // 检查是否为GZIP格式
                    boolean isGzip = responseBytes.length >= 2 && 
                        responseBytes[0] == (byte) 0x1f && responseBytes[1] == (byte) 0x8b;
                    
                    log.info("响应体大小: {} bytes, 是否为GZIP: {}", responseBytes.length, isGzip);
                    
                    if (isGzip) {
                        // 解压缩 GZIP 响应体
                        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes))) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = gzipInputStream.read(buffer)) != -1) {
                                byteArrayOutputStream.write(buffer, 0, length);
                            }
                            responseBody = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            log.error("GZIP 解压失败，原始响应体: {}", new String(responseBytes, StandardCharsets.UTF_8), e);
                            return FQNovelResponse.error("GZIP解压失败: " + e.getMessage());
                        }
                    } else {
                        // 直接使用原始响应体
                        responseBody = new String(responseBytes, StandardCharsets.UTF_8);
                    }
                    
                    // 检查响应体是否为空
                    if (responseBody.trim().isEmpty()) {
                        log.error("解压后的响应体为空");
                        if (attempt < maxAttempts - 1) {
                            log.warn("解压后响应体为空，准备重试 - 第{}次", attempt + 1);
                            try {
                                Thread.sleep(1000 * (attempt + 1));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                        return FQNovelResponse.error("API响应内容为空");
                    }
                    
                    // 检查是否为错误响应
                    if (responseBody.contains("\"code\":110") || responseBody.contains("ILLEGAL_ACCESS")) {
                        log.error("API返回访问错误: {}", responseBody);
                        if (attempt < maxAttempts - 1) {
                            log.warn("检测到访问错误，尝试刷新registerkey并重试 - 第{}次", attempt + 1);
                            try {
                                registerKeyService.refreshRegisterKey();
                                Thread.sleep(2000); // 等待2秒后重试
                            } catch (Exception e) {
                                log.error("刷新registerkey失败", e);
                            }
                            continue;
                        }
                        return FQNovelResponse.error("API访问被拒绝，请检查请求参数和认证信息");
                    }

                    // 解析响应
                    FqIBatchFullResponse batchResponse = objectMapper.readValue(responseBody, FqIBatchFullResponse.class);
                    log.info("成功获取批量章节内容 - itemIds: {}, 章节数量: {}", 
                        itemIds, batchResponse.getData() != null ? batchResponse.getData().size() : 0);

                    // 构建原始响应信息（如果需要）
                    FQBatchChapterResponse.RawApiResponse rawApiResponse = null;
                    if (includeRawResponse) {
                        rawApiResponse = new FQBatchChapterResponse.RawApiResponse();
                        rawApiResponse.setHttpStatus(response.getStatusCode().value());
                        Map<String, String> responseHeaders = new HashMap<>();
                        response.getHeaders().forEach((key, values) -> {
                            if (!values.isEmpty()) {
                                responseHeaders.put(key, String.join(", ", values));
                            }
                        });
                        rawApiResponse.setHeaders(responseHeaders);
                        rawApiResponse.setRawBody(new String(responseBytes, StandardCharsets.UTF_8));
                        rawApiResponse.setBodySize(responseBytes.length);
                        rawApiResponse.setIsGzip(isGzip);
                        rawApiResponse.setDecompressedBody(responseBody);
                        rawApiResponse.setRequestUrl(fullUrl);
                        rawApiResponse.setRequestTimestamp(requestTimestamp);
                        rawApiResponse.setResponseTimestamp(responseTimestamp);
                    }

                    // 成功时重置失败计数
                    consecutiveFailures = 0;
                    return FQNovelResponse.success(new BatchFullResponseWithRaw(batchResponse, rawApiResponse));

                } catch (Exception e) {
                    consecutiveFailures++; // 增加失败计数
                    log.error("批量获取章节内容失败 - 第{}次尝试, itemIds: {}, 连续失败次数: {}", 
                        attempt + 1, itemIds, consecutiveFailures, e);
                    if (attempt < maxAttempts - 1) {
                        log.warn("准备重试 - 第{}次", attempt + 1);
                        try {
                            Thread.sleep(1000 * (attempt + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    return FQNovelResponse.error("批量获取章节内容失败: " + e.getMessage());
                }
            }
            return FQNovelResponse.error("批量获取章节内容失败: 超过最大重试次数");
        });
    }

/*
    public CompletableFuture<FQNovelResponse<FqIBatchFullResponse>> batchFull(String itemIds, String bookId, boolean download) {
        return CompletableFuture.supplyAsync(() -> {
            int maxAttempts = 1;
            for (int attempt = 0; attempt <= maxAttempts; attempt++) {
                try {
                    FqVariable var = getFqVariable();
                    String url = fqApiUtils.getBaseUrl() + "/reading/reader/batch_full/v";
                    Map<String, String> params = fqApiUtils.buildBatchFullParams(var, itemIds, bookId, download);
                    String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

                    Map<String, String> headers = fqApiUtils.buildCommonHeaders();
                    Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

                    HttpHeaders httpHeaders = new HttpHeaders();
                    signedHeaders.forEach(httpHeaders::set);
                    headers.forEach(httpHeaders::set);

                    HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
                    ResponseEntity<byte[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, byte[].class);

                    byte[] body = response.getBody();
                    boolean isGzip = false;
                    List<String> contentEncoding = response.getHeaders().get("Content-Encoding");
                    if (contentEncoding != null) {
                        isGzip = contentEncoding.stream().anyMatch(e -> e.toLowerCase().contains("gzip"));
                    }
                    // 简单判断GZIP头
                    if (!isGzip && body != null && body.length >= 2 && body[0] == (byte)0x1f && body[1] == (byte)0x8b) {
                        isGzip = true;
                    }

                    if (!isGzip) {
                        // 非GZIP，解析JSON
                        String rawBody = new String(body, StandardCharsets.UTF_8);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(rawBody);
                        int code = node.has("code") ? node.get("code").asInt() : -1;
                        String message = node.has("message") ? node.get("message").asText() : "";
                        if (code == 110 && "ILLEGAL_ACCESS".equals(message)) {
                            log.warn("检测到ILLEGAL_ACCESS，尝试刷新registerkey，第{}次", attempt);
                            try {
                                registerKeyService.refreshRegisterKey();
                            } catch (Exception e) {
                                log.error("刷新registerkey失败", e);
                                return FQNovelResponse.error("刷新registerkey失败: " + e.getMessage());
                            }
                            continue; // 重试
                        } else {
                            // 非非法访问，直接返回对应code和message
                            return FQNovelResponse.error("code: " + code + ", message: " + message);
                        }
                    }

                    // GZIP解压
                    String responseBody = "";
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(body))) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = gzipInputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, length);
                        }
                        responseBody = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        log.error("GZIP 解压失败", e);
                    }

                    FqIBatchFullResponse batchResponse = objectMapper.readValue(responseBody, FqIBatchFullResponse.class);
                    return FQNovelResponse.success(batchResponse);

                } catch (Exception e) {
                    log.error("批量获取章节内容失败 - itemIds: {}", itemIds, e);
                    return FQNovelResponse.error("批量获取章节内容失败: " + e.getMessage());
                }
            }
            return FQNovelResponse.error("批量获取章节内容失败: 超过最大重试次数");
        });
    }
*/

    /**
     * 获取书籍信息 (从目录接口获取完整信息)
     *
     * @param bookId 书籍ID
     * @return 书籍信息
     */
    public CompletableFuture<FQNovelResponse<FQNovelBookInfo>> getBookInfo(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证bookId参数
                if (bookId == null || bookId.trim().isEmpty()) {
                    return FQNovelResponse.error("书籍ID不能为空");
                }

                // 构建目录请求
                FQDirectoryRequest directoryRequest = new FQDirectoryRequest();
                directoryRequest.setBookId(bookId);
                directoryRequest.setBookType(0);
                directoryRequest.setNeedVersion(true);

                // 调用目录接口获取书籍信息
                FQNovelResponse<FQDirectoryResponse> directoryResponse = fqSearchService.getBookDirectory(directoryRequest).get();

                if (directoryResponse.getCode() != 0 || directoryResponse.getData() == null) {
                    return FQNovelResponse.error("获取书籍目录失败: " + directoryResponse.getMessage());
                }

                FQDirectoryResponse directoryData = directoryResponse.getData();
                FQNovelBookInfoResp bookInfoResp = directoryData.getBookInfo();

                if (bookInfoResp == null) {
                    return FQNovelResponse.error("书籍信息不存在");
                }

                // 从FQNovelBookInfoResp转换为FQNovelBookInfo
                FQNovelBookInfo bookInfo = new FQNovelBookInfo();
                bookInfo.setBookId(bookId);
                bookInfo.setBookName(bookInfoResp.getBookName());
                bookInfo.setAuthor(bookInfoResp.getAuthor());
                bookInfo.setDescription(bookInfoResp.getAbstractContent());
                bookInfo.setCoverUrl(bookInfoResp.getThumbUrl());
                bookInfo.setWordNumber(bookInfoResp.getWordNumber());
                bookInfo.setTags(bookInfoResp.getTags());
                bookInfo.setLastChapterTitle(bookInfoResp.getLastChapterTitle());

                // 状态转换 (假设status字段表示连载状态)
                if (bookInfoResp.getStatus() != null) {
                    try {
                        bookInfo.setStatus(Integer.parseInt(bookInfoResp.getStatus()));
                    } catch (NumberFormatException e) {
                        bookInfo.setStatus(0); // 默认为连载中
                    }
                } else {
                    bookInfo.setStatus(0);
                }

                // 章节总数
                if (bookInfoResp.getContentChapterNumber() != null) {
                    try {
                        bookInfo.setTotalChapters(Integer.parseInt(bookInfoResp.getContentChapterNumber()));
                    } catch (NumberFormatException e) {
                        // 如果无法解析，从目录数据获取
                        List<FQDirectoryResponse.CatalogItem> catalogData = directoryData.getCatalogData();
                        bookInfo.setTotalChapters(catalogData != null ? catalogData.size() : 0);
                    }
                } else {
                    // 从目录数据获取章节总数
                    List<FQDirectoryResponse.CatalogItem> catalogData = directoryData.getCatalogData();
                    bookInfo.setTotalChapters(catalogData != null ? catalogData.size() : 0);
                }

                return FQNovelResponse.success(bookInfo);

            } catch (Exception e) {
                log.error("获取书籍信息失败 - bookId: {}", bookId, e);
                return FQNovelResponse.error("获取书籍信息失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取解密的章节内容
     *
     * @param itemIds 章节ID列表，逗号分隔
     * @param bookId 书籍ID
     * @param download 是否下载模式
     * @return 解密后的章节内容列表
     */
    public CompletableFuture<FQNovelResponse<List<Map.Entry<String, String>>>> getDecryptedContents(String itemIds, String bookId, boolean download) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 先获取批量内容
                FQNovelResponse<FqIBatchFullResponse> batchResponse = batchFull(itemIds, bookId, download).get();

                if (batchResponse.getCode() != 0 || batchResponse.getData() == null) {
                    return FQNovelResponse.error("获取批量内容失败: " + batchResponse.getMessage());
                }

                // 解密内容
                List<Map.Entry<String, String>> decryptedContents =
                    batchResponse.getData().getDecryptContents(registerKeyService);

                return FQNovelResponse.success(decryptedContents);

            } catch (Exception e) {
                log.error("获取解密章节内容失败 - itemIds: {}", itemIds, e);
                return FQNovelResponse.error("获取解密章节内容失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取章节内容 (使用新的API模式)
     *
     * @param request 包含书籍ID和章节ID的请求
     * @return 章节内容
     */
    public CompletableFuture<FQNovelResponse<FQNovelChapterInfo>> getChapterContent(FQNovelRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (request.getBookId() == null || request.getChapterId() == null) {
                    return FQNovelResponse.error("书籍ID和章节ID不能为空");
                }

                // 使用batch_full API获取完整响应数据
                String itemIds = request.getChapterId();
                FQNovelResponse<FqIBatchFullResponse> batchResponse = batchFull(itemIds, request.getBookId(), false).get();

                if (batchResponse.getCode() != 0 || batchResponse.getData() == null) {
                    return FQNovelResponse.error("获取章节内容失败: " + batchResponse.getMessage());
                }

                FqIBatchFullResponse batchFullResponse = batchResponse.getData();
                Map<String, ItemContent> dataMap = batchFullResponse.getData();

                if (dataMap == null || dataMap.isEmpty()) {
                    return FQNovelResponse.error("未找到章节数据");
                }

                // 获取第一个章节的内容
                String chapterId = request.getChapterId();
                ItemContent itemContent = dataMap.get(chapterId);

                if (itemContent == null) {
                    // 如果使用chapterId没找到，尝试使用第一个可用的key
                    itemContent = dataMap.values().iterator().next();
                    chapterId = dataMap.keySet().iterator().next();
                }

                if (itemContent == null) {
                    return FQNovelResponse.error("未找到章节内容");
                }

                // 解密章节内容
                String decryptedContent = "";
                try {
                    Long contentKeyver = itemContent.getKeyVersion();
                    String key = registerKeyService.getDecryptionKey(contentKeyver);
                    decryptedContent = FqCrypto.decryptAndDecompressContent(itemContent.getContent(), key);
                } catch (Exception e) {
                    log.error("解密章节内容失败 - chapterId: {}", chapterId, e);
                    return FQNovelResponse.error("解密章节内容失败: " + e.getMessage());
                }

                // 从HTML中提取纯文本内容
                String txtContent = extractTextFromHtml(decryptedContent);

                // 构建章节信息对象
                FQNovelChapterInfo chapterInfo = new FQNovelChapterInfo();
                chapterInfo.setChapterId(chapterId);
                chapterInfo.setBookId(request.getBookId());
                chapterInfo.setRawContent(decryptedContent);
                chapterInfo.setTxtContent(txtContent);

                // 从ItemContent中提取标题
                String title = itemContent.getTitle();
                if (title == null || title.trim().isEmpty()) {
                    // 如果title为空，尝试从HTML中提取标题
                    Pattern titlePattern = Pattern.compile("<h1[^>]*>.*?<blk[^>]*>([^<]*)</blk>.*?</h1>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                    Matcher titleMatcher = titlePattern.matcher(decryptedContent);
                    if (titleMatcher.find()) {
                        title = titleMatcher.group(1).trim();
                    } else {
                        title = "章节标题";
                    }
                }
                chapterInfo.setTitle(title);

                // 从novelData中提取作者信息（如果可用）
                FQNovelData novelData = itemContent.getNovelData();
                chapterInfo.setAuthorName(novelData != null ? novelData.getAuthor() : "未知作者");
                // 设置其他字段
                chapterInfo.setWordCount(txtContent.length());
                chapterInfo.setUpdateTime(System.currentTimeMillis());

                return FQNovelResponse.success(chapterInfo);

            } catch (Exception e) {
                log.error("获取章节内容失败 - bookId: {}, chapterId: {}",
                    request.getBookId(), request.getChapterId(), e);
                return FQNovelResponse.error("获取章节内容失败: " + e.getMessage());
            }
        });
    }

    /**
     * 从HTML内容中提取纯文本
     * 主要提取 <blk> 标签中的文本内容，按照 e_order 排序
     *
     * @param htmlContent HTML内容
     * @return 提取的纯文本内容
     */
    private String extractTextFromHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        StringBuilder textBuilder = new StringBuilder();

        try {
            // 使用正则表达式提取 <blk> 标签中的文本内容
            Pattern blkPattern = Pattern.compile("<blk[^>]*>([^<]*)</blk>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = blkPattern.matcher(htmlContent);

            while (matcher.find()) {
                String text = matcher.group(1);
                if (text != null && !text.trim().isEmpty()) {
                    textBuilder.append(text.trim()).append("\n");
                }
            }

            // 如果没有找到 <blk> 标签，尝试提取所有文本内容
            if (textBuilder.length() == 0) {
                // 简单的HTML标签移除，保留文本内容
                String text = htmlContent.replaceAll("<[^>]+>", "").trim();
                if (!text.isEmpty()) {
                    textBuilder.append(text);
                }
            }

        } catch (Exception e) {
            log.warn("HTML文本提取失败，返回原始内容", e);
            // 如果解析失败，返回去除HTML标签的简单文本
            return htmlContent.replaceAll("<[^>]+>", "").trim();
        }

        return textBuilder.toString().trim();
    }

    /**
     * 批量获取章节内容 (新功能)
     *
     * @param request 批量章节请求
     * @return 批量章节响应
     */
    public CompletableFuture<FQNovelResponse<FQBatchChapterResponse>> getBatchChapterContent(FQBatchChapterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证参数
                if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
                    return FQNovelResponse.error("书籍ID不能为空");
                }

                if ((request.getChapterRange() == null || request.getChapterRange().trim().isEmpty())&& request.getChapterIds() == null) {
                    return FQNovelResponse.error("章节范围或章节ids不能为空");
                }

                List<String> itemIds = new ArrayList<>();
                List<String> chapterIds;

                if (request.getChapterIds() != null && !request.getChapterIds().isEmpty()) {
                    // 如果提供了章节ID列表，直接使用
                    itemIds = request.getChapterIds();
                    chapterIds = request.getChapterIds();
                } else {
                    // 否则使用章节范围字符串
                    chapterIds = parseChapterRange(request.getChapterRange());
                    if (chapterIds.isEmpty()) {
                        return FQNovelResponse.error("无效的章节范围格式");
                    }

                    // 验证章节数量限制 (1-30)
                    if (chapterIds.size() < 1 || chapterIds.size() > 30) {
                        return FQNovelResponse.error("章节数量必须在1-30之间，当前请求: " + chapterIds.size());
                    }

                    if (isChapterPositions(chapterIds)) {
                        // 输入是章节位置(如1,2,3)，需要通过目录API获取实际的itemIds
                        itemIds = getItemIdsByChapterPositions(request.getBookId(), chapterIds);
                    }
                }

                if (itemIds.isEmpty()) {
                    return FQNovelResponse.error("无法获取章节对应的itemIds，请检查章节范围是否有效");
                }

                // 调用批量获取API
                String itemIdsStr = String.join(",", itemIds);
                boolean includeRawResponse = request.getRawResponse() != null && request.getRawResponse();
                FQNovelResponse<BatchFullResponseWithRaw> batchResponse = batchFullWithRaw(itemIdsStr, request.getBookId(), true, includeRawResponse).get();

                if (batchResponse.getCode() != 0 || batchResponse.getData() == null) {
                    return FQNovelResponse.error("获取批量章节内容失败: " + batchResponse.getMessage());
                }

                FqIBatchFullResponse batchFullResponse = batchResponse.getData().getBatchResponse();
                Map<String, ItemContent> dataMap = batchFullResponse.getData();

                if (dataMap == null) {
                    dataMap = new HashMap<>();
                }

                // 构建响应
                FQBatchChapterResponse response = new FQBatchChapterResponse();
                response.setBookId(request.getBookId());
                response.setRequestedRange(request.getChapterRange());
                response.setTotalRequested(chapterIds.size());
                // 获取第一个itemId的novelData信息
                FQNovelData novelData = dataMap.get(itemIds.get(0)).getNovelData();

                // 构建书籍信息 (简化版本)
                FQNovelBookInfo bookInfo = new FQNovelBookInfo();
                bookInfo.setBookId(request.getBookId());
                bookInfo.setBookName(novelData.getBookName());
                bookInfo.setAuthor(novelData.getAuthor());
                bookInfo.setCoverUrl(novelData.getThumbUrl());
                bookInfo.setStatus(novelData.getStatus());
                bookInfo.setTotalChapters(novelData.getWordNumber());
                response.setBookInfo(bookInfo);

                // 处理每个章节
                Map<String, FQBatchChapterInfo> chaptersMap = new LinkedHashMap<>();
                int successCount = 0;

                for (String itemId : itemIds) {
                    try {
                        ItemContent itemContent = dataMap.get(itemId);

                        if (itemContent == null) {
                            log.warn("未找到章节内容 - itemId: {}", itemId);
                            continue;
                        }

                        // 解密章节内容
                        String decryptedContent = "";
                        try {
                            Long contentKeyver = itemContent.getKeyVersion();
                            String key = registerKeyService.getDecryptionKey(contentKeyver);
                            decryptedContent = FqCrypto.decryptAndDecompressContent(itemContent.getContent(), key);
                        } catch (Exception e) {
                            log.error("解密章节内容失败 - itemId: {}", itemId, e);
                            continue;
                        }

                        // 提取纯文本内容
                        String txtContent = extractTextFromHtml(decryptedContent);

                        // 提取章节标题
                        String title = itemContent.getTitle();
                        if (title == null || title.trim().isEmpty()) {
                            // 从HTML中提取标题
                            Pattern titlePattern = Pattern.compile("<h1[^>]*>.*?<blk[^>]*>([^<]*)</blk>.*?</h1>",
                                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                            Matcher titleMatcher = titlePattern.matcher(decryptedContent);
                            if (titleMatcher.find()) {
                                title = titleMatcher.group(1).trim();
                            } else {
                                title = "章节 " + itemId;
                            }
                        }

                        // 构建章节信息
                        FQBatchChapterInfo chapterInfo = new FQBatchChapterInfo();
                        chapterInfo.setChapterName(title);
                        chapterInfo.setRawContent(decryptedContent);
                        chapterInfo.setTxtContent(txtContent);
                        chapterInfo.setWordCount(txtContent.length());
                        chapterInfo.setIsFree(true); // 默认为免费，可以后续扩展

                        // 使用对应的章节位置作为key（如果是章节位置模式）
                        String chapterKey;
                        if (isChapterPositions(chapterIds)) {
                            // 找到这个itemId对应的章节位置
                            int itemIndex = itemIds.indexOf(itemId);
                            if (itemIndex >= 0 && itemIndex < chapterIds.size()) {
                                chapterKey = chapterIds.get(itemIndex);
                            } else {
                                chapterKey = itemId;
                            }
                        } else {
                            chapterKey = itemId;
                        }

                        chaptersMap.put(chapterKey, chapterInfo);
                        successCount++;

                    } catch (Exception e) {
                        log.error("处理章节失败 - itemId: {}", itemId, e);
                    }
                }

                response.setChapters(chaptersMap);
                response.setSuccessCount(successCount);

                // 添加原始API响应信息（如果需要）
                if (includeRawResponse && batchResponse.getData() != null && batchResponse.getData().getRawApiResponse() != null) {
                    response.setRawApiResponse(batchResponse.getData().getRawApiResponse());
                }

                return FQNovelResponse.success(response);

            } catch (Exception e) {
                log.error("批量获取章节内容失败 - bookId: {}, range: {}",
                    request.getBookId(), request.getChapterRange(), e);
                return FQNovelResponse.error("批量获取章节内容失败: " + e.getMessage());
            }
        });
    }

    /**
     * 解析章节范围字符串
     * 支持格式: "1-30", "5", "5-5"
     *
     * @param rangeStr 章节范围字符串
     * @return 章节ID列表
     */
    private List<String> parseChapterRange(String rangeStr) {
        List<String> chapterIds = new ArrayList<>();

        try {
            rangeStr = rangeStr.trim();

            if (rangeStr.contains("-")) {
                // 范围格式: "1-30"
                String[] parts = rangeStr.split("-");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());

                    if (start <= end && start > 0 && end > 0) {
                        for (int i = start; i <= end; i++) {
                            chapterIds.add(String.valueOf(i));
                        }
                    }
                }
            } else {
                // 单个章节格式: "5"
                int chapterNum = Integer.parseInt(rangeStr);
                if (chapterNum > 0) {
                    chapterIds.add(String.valueOf(chapterNum));
                }
            }
        } catch (NumberFormatException e) {
            log.error("解析章节范围失败: {}", rangeStr, e);
        }

        return chapterIds;
    }

    /**
     * 判断输入的ID列表是否为章节位置（而非itemIds）
     * 章节位置通常是小的数字（1, 2, 3等），而itemIds是长字符串
     *
     * @param ids ID列表
     * @return true如果是章节位置，false如果是itemIds
     */
    private boolean isChapterPositions(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 检查所有ID是否都是小的正整数（通常章节位置不会超过10000）
        for (String id : ids) {
            try {
                int num = Integer.parseInt(id);
                if (num <= 0 || num > 10000) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // 如果无法解析为数字，说明可能是itemId（长字符串）
                return false;
            }
        }

        return true;
    }

    /**
     * 根据章节位置获取对应的itemIds
     *
     * @param bookId 书籍ID
     * @param chapterPositions 章节位置列表（如["1", "2", "3"]）
     * @return 对应的itemIds列表
     */
    private List<String> getItemIdsByChapterPositions(String bookId, List<String> chapterPositions) {
        List<String> itemIds = new ArrayList<>();

        try {
            // 构建目录请求
            FQDirectoryRequest directoryRequest = new FQDirectoryRequest();
            directoryRequest.setBookId(bookId);
            directoryRequest.setBookType(0);
            directoryRequest.setNeedVersion(true);

            // 获取书籍目录
            FQNovelResponse<FQDirectoryResponse> directoryResponse = fqSearchService.getBookDirectory(directoryRequest).get();

            if (directoryResponse.getCode() != 0 || directoryResponse.getData() == null) {
                log.error("获取书籍目录失败 - bookId: {}, error: {}", bookId, directoryResponse.getMessage());
                return itemIds;
            }

            List<FQDirectoryResponse.CatalogItem> catalogItems = directoryResponse.getData().getCatalogData();
            if (catalogItems == null || catalogItems.isEmpty()) {
                log.error("书籍目录为空 - bookId: {}", bookId);
                return itemIds;
            }

            // 构建章节位置到itemId的映射
            // 目录中的章节按顺序排列，第1章对应索引0，第2章对应索引1，以此类推
            for (String positionStr : chapterPositions) {
                try {
                    int position = Integer.parseInt(positionStr);
                    int index = position - 1; // 转换为0基索引

                    if (index >= 0 && index < catalogItems.size()) {
                        String itemId = catalogItems.get(index).getItemId();
                        if (itemId != null && !itemId.trim().isEmpty()) {
                            itemIds.add(itemId);
                        } else {
                            log.warn("章节位置 {} 对应的itemId为空 - bookId: {}", position, bookId);
                        }
                    } else {
                        log.warn("章节位置 {} 超出范围，总章节数: {} - bookId: {}", position, catalogItems.size(), bookId);
                    }
                } catch (NumberFormatException e) {
                    log.error("无效的章节位置: {} - bookId: {}", positionStr, bookId, e);
                }
            }

        } catch (Exception e) {
            log.error("获取章节itemIds失败 - bookId: {}, positions: {}", bookId, chapterPositions, e);
        }

        return itemIds;
    }
}
