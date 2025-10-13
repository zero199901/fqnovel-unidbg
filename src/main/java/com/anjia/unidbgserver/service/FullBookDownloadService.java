package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 全本下载服务
 */
@Slf4j
@Service
public class FullBookDownloadService {

    @Autowired
    private FQNovelService fqNovelService;
    
    @Autowired
    private FQSearchService fqSearchService;
    
    @Autowired
    private RedisService redisService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 全本下载（流式返回）
     */
    public Flux<FullBookDownloadResponse> downloadFullBook(FullBookDownloadRequest request) {
        return Flux.create(sink -> {
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("开始全本下载 - bookId: {}, batchSize: {}", request.getBookId(), request.getBatchSize());
                    
                    // 1. 获取书籍信息（优先从Redis获取）
                    FQNovelBookInfo bookInfo = redisService.getBookInfo(request.getBookId());
                    if (bookInfo == null) {
                        log.info("Redis中未找到作品信息，从API获取 - bookId: {}", request.getBookId());
                        FQNovelResponse<FQNovelBookInfo> bookResponse = fqNovelService.getBookInfo(request.getBookId()).get();
                        if (bookResponse.getCode() != 0 || bookResponse.getData() == null) {
                            sink.error(new RuntimeException("获取书籍信息失败: " + bookResponse.getMessage()));
                            return;
                        }
                        bookInfo = bookResponse.getData();
                        
                        // 保存到Redis
                        redisService.saveBookInfo(request.getBookId(), bookInfo);
                        log.info("作品信息已保存到Redis - bookId: {}", request.getBookId());
                    } else {
                        log.info("从Redis获取作品信息成功 - bookId: {}", request.getBookId());
                    }
                    // 优先以目录真实章节总数为准，避免误把 wordNumber 当作章节数
                    List<String> allChapterIds = getBookChapterIds(request.getBookId()).get();
                    if (allChapterIds == null) {
                        allChapterIds = new ArrayList<>();
                    }
                    int totalChapters = allChapterIds.size();
                    log.info("书籍信息获取成功 - 书名: {}, 目录章节数: {}", bookInfo.getBookName(), totalChapters);

                    // 2. 计算实际要下载的章节数（maxChapters 允许为 null）
                    Integer reqMax = request.getMaxChapters();
                    int maxChapters = (reqMax != null && reqMax > 0) ? reqMax : totalChapters;
                    int actualChapters = Math.min(maxChapters, totalChapters);
                    
                    // 3. 分批下载章节
                    int batchSize = request.getBatchSize();
                    int totalBatches = (int) Math.ceil((double) actualChapters / batchSize);
                    int downloadedChapters = 0;
                    
                    for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                        try {
                            // 计算当前批次的章节范围
                            int startIndex = request.getStartIndex() + batchIndex * batchSize;
                            int endIndex = Math.min(startIndex + batchSize, actualChapters);
                            
                            if (startIndex >= actualChapters) {
                                break;
                            }
                            
                            log.info("开始下载第 {} 批章节 - 范围: {}-{}", batchIndex + 1, startIndex, endIndex - 1);
                            
                            // 复用预先获取的章节ID
                            if (allChapterIds.isEmpty()) {
                                log.warn("第 {} 批章节获取失败：无法获取章节ID列表", batchIndex + 1);
                                continue;
                            }
                            
                            // 计算当前批次的章节范围
                            int actualStartIndex = Math.min(startIndex, allChapterIds.size());
                            int actualEndIndex = Math.min(endIndex, allChapterIds.size());
                            
                            if (actualStartIndex >= allChapterIds.size()) {
                                log.info("已处理完所有章节");
                                break;
                            }
                            
                            // 获取当前批次的章节ID，并过滤掉已存在的章节
                            List<String> chapterIds = new ArrayList<>();
                            List<String> skippedChapterIds = new ArrayList<>();
                            
                            for (int i = actualStartIndex; i < actualEndIndex; i++) {
                                String chapterId = allChapterIds.get(i);
                                
                                // 检查Redis中是否已存在该章节
                                if (request.getSaveToRedis() && redisService.hasChapter(request.getBookId(), chapterId)) {
                                    skippedChapterIds.add(chapterId);
                                    log.debug("章节已存在，跳过 - bookId: {}, chapterId: {}", request.getBookId(), chapterId);
                                } else {
                                    chapterIds.add(chapterId);
                                }
                            }
                            
                            // 如果所有章节都已存在，跳过当前批次
                            if (chapterIds.isEmpty()) {
                                downloadedChapters += actualEndIndex - actualStartIndex;
                                log.info("第 {} 批所有章节都已存在，跳过 - 跳过数量: {}", batchIndex + 1, skippedChapterIds.size());
                                
                                // 发送跳过响应
                                FullBookDownloadResponse skipResponse = FullBookDownloadResponse.progress(
                                    request.getBookId(),
                                    bookInfo,
                                    actualChapters,
                                    downloadedChapters,
                                    0,
                                    batchIndex + 1,
                                    totalBatches,
                                    new HashMap<>(),
                                    skippedChapterIds,
                                    String.format("第 %d/%d 批章节已存在，跳过 %d 章", batchIndex + 1, totalBatches, skippedChapterIds.size())
                                );
                                sink.next(skipResponse);
                                continue;
                            }
                            
                            log.info("第 {} 批章节 - 需要下载: {}, 跳过: {}", batchIndex + 1, chapterIds.size(), skippedChapterIds.size());
                            
                            // 构建批量章节请求
                            FQBatchChapterRequest batchRequest = new FQBatchChapterRequest();
                            batchRequest.setBookId(request.getBookId());
                            batchRequest.setChapterIds(chapterIds);
                            
                            // 获取章节内容
                            FQNovelResponse<FQBatchChapterResponse> batchResponse = 
                                fqNovelService.getBatchChapterContent(batchRequest).get();
                            
                            if (batchResponse.getCode() != 0 || batchResponse.getData() == null) {
                                log.warn("第 {} 批章节下载失败: {}", batchIndex + 1, batchResponse.getMessage());
                                continue;
                            }
                            
                            FQBatchChapterResponse batchData = batchResponse.getData();
                            Map<String, FQBatchChapterInfo> batchChapters = batchData.getChapters();
                            
                            // 转换为FQNovelChapterInfo
                            Map<String, FQNovelChapterInfo> chapters = new HashMap<>();
                            if (batchChapters != null) {
                                for (Map.Entry<String, FQBatchChapterInfo> entry : batchChapters.entrySet()) {
                                    FQNovelChapterInfo chapterInfo = new FQNovelChapterInfo();
                                    chapterInfo.setTitle(entry.getValue().getChapterName());
                                    chapterInfo.setRawContent(entry.getValue().getRawContent());
                                    chapterInfo.setTxtContent(entry.getValue().getTxtContent());
                                    chapterInfo.setWordCount(entry.getValue().getWordCount());
                                    chapterInfo.setChapterIndex(entry.getValue().getChapterIndex());
                                    chapterInfo.setIsFree(entry.getValue().getIsFree());
                                    chapters.put(entry.getKey(), chapterInfo);
                                }
                            }
                            
                            // 保存到Redis
                            if (request.getSaveToRedis() && chapters != null) {
                                for (Map.Entry<String, FQNovelChapterInfo> entry : chapters.entrySet()) {
                                    redisService.saveChapter(request.getBookId(), entry.getKey(), entry.getValue());
                                }
                            }
                            
                            // 计算本批次处理的章节总数（包括下载的和跳过的）
                            int currentBatchProcessed = (chapters != null ? chapters.size() : 0) + skippedChapterIds.size();
                            downloadedChapters += currentBatchProcessed;
                            
                            // 合并章节ID列表（包括跳过的）
                            List<String> allChapterIdsInBatch = new ArrayList<>(chapterIds);
                            allChapterIdsInBatch.addAll(skippedChapterIds);
                            
                            // 发送进度响应
                            FullBookDownloadResponse response = FullBookDownloadResponse.progress(
                                request.getBookId(),
                                bookInfo,
                                actualChapters,
                                downloadedChapters,
                                currentBatchProcessed,
                                batchIndex + 1,
                                totalBatches,
                                chapters,
                                allChapterIdsInBatch,
                                String.format("第 %d/%d 批章节处理完成 - 下载: %d, 跳过: %d", 
                                    batchIndex + 1, totalBatches, 
                                    chapters != null ? chapters.size() : 0, 
                                    skippedChapterIds.size())
                            );
                            
                            sink.next(response);
                            
                            // 如果完成，发送最终响应
                            if (downloadedChapters >= actualChapters) {
                                FullBookDownloadResponse finalResponse = FullBookDownloadResponse.completed(
                                    request.getBookId(),
                                    bookInfo,
                                    actualChapters,
                                    "全本下载完成"
                                );
                                sink.next(finalResponse);
                                break;
                            }
                            
                            // 添加延迟避免请求过快
                            Thread.sleep(1000);
                            
                        } catch (Exception e) {
                            log.error("第 {} 批章节下载异常", batchIndex + 1, e);
                            sink.error(e);
                            return;
                        }
                    }
                    
                    sink.complete();
                    
                } catch (Exception e) {
                    log.error("全本下载异常", e);
                    sink.error(e);
                }
            }, executorService);
        });
    }

    /**
     * 获取已下载的章节列表
     */
    public CompletableFuture<List<FQNovelChapterInfo>> getDownloadedChapters(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Set<String> chapterIds = redisService.getBookChapterIds(bookId);
                if (chapterIds == null || chapterIds.isEmpty()) {
                    return new ArrayList<>();
                }
                
                List<FQNovelChapterInfo> chapters = new ArrayList<>();
                for (String chapterId : chapterIds) {
                    FQNovelChapterInfo chapter = redisService.getChapter(bookId, chapterId);
                    if (chapter != null) {
                        chapters.add(chapter);
                    }
                }
                
                // 按章节ID排序
                chapters.sort(Comparator.comparing(chapter -> chapter.getTitle()));
                
                return chapters;
                
            } catch (Exception e) {
                log.error("获取已下载章节列表失败 - bookId: {}", bookId, e);
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * 获取下载进度
     */
    public CompletableFuture<Map<String, Object>> getDownloadProgress(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 优先从Redis获取书籍信息
                FQNovelBookInfo bookInfo = redisService.getBookInfo(bookId);
                if (bookInfo == null) {
                    // 从API获取
                    FQNovelResponse<FQNovelBookInfo> bookResponse = fqNovelService.getBookInfo(bookId).get();
                    if (bookResponse.getCode() != 0 || bookResponse.getData() == null) {
                        return Collections.singletonMap("error", "获取书籍信息失败");
                    }
                    bookInfo = bookResponse.getData();
                }
                int totalChapters = bookInfo.getTotalChapters();
                long downloadedCount = redisService.getBookDownloadedChapterCount(bookId);

                // 回退策略：如果总章节数为0或未设置，则重新拉取并从目录计算
                if (totalChapters <= 0) {
                    try {
                        FQNovelResponse<FQNovelBookInfo> fresh = fqNovelService.getBookInfo(bookId).get();
                        if (fresh.getCode() == 0 && fresh.getData() != null) {
                            bookInfo = fresh.getData();
                            totalChapters = bookInfo.getTotalChapters();
                        }
                    } catch (Exception ignored) {
                    }

                    if (totalChapters <= 0) {
                        try {
                            List<String> chapterIds = getBookChapterIds(bookId).get();
                            if (chapterIds != null) {
                                totalChapters = chapterIds.size();
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    // 最后兜底：至少不小于已下载数量，避免出现 completed=true 但 total=0 的矛盾
                    if (totalChapters <= 0 && downloadedCount > 0) {
                        totalChapters = (int) downloadedCount;
                    }
                }
                
                Map<String, Object> progress = new HashMap<>();
                progress.put("bookId", bookId);
                progress.put("bookName", bookInfo.getBookName());
                progress.put("totalChapters", totalChapters);
                progress.put("downloadedChapters", downloadedCount);
                progress.put("progress", totalChapters > 0 ? (double) downloadedCount / totalChapters * 100 : 0);
                progress.put("completed", downloadedCount >= totalChapters);
                progress.put("timestamp", System.currentTimeMillis());
                
                return progress;
                
            } catch (Exception e) {
                log.error("获取下载进度失败 - bookId: {}", bookId, e);
                return Collections.singletonMap("error", "获取下载进度失败: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 删除已下载的章节
     */
    public CompletableFuture<Boolean> deleteDownloadedChapters(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                redisService.deleteBookChapters(bookId);
                log.info("已下载章节删除成功 - bookId: {}", bookId);
                return true;
            } catch (Exception e) {
                log.error("删除已下载章节失败 - bookId: {}", bookId, e);
                return false;
            }
        }, executorService);
    }

    /**
     * 获取书籍的章节ID列表（优先从Redis获取）
     */
    private CompletableFuture<List<String>> getBookChapterIds(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 优先从Redis获取章节列表
                List<String> chapterIds = redisService.getChapterList(bookId);
                if (chapterIds != null && !chapterIds.isEmpty()) {
                    log.info("从Redis获取章节列表成功 - bookId: {}, 章节数量: {}", bookId, chapterIds.size());
                    return chapterIds;
                }
                
                log.info("Redis中未找到章节列表，从API获取 - bookId: {}", bookId);
                
                // 构建目录请求
                FQDirectoryRequest directoryRequest = new FQDirectoryRequest();
                directoryRequest.setBookId(bookId);
                directoryRequest.setBookType(0);
                directoryRequest.setNeedVersion(true);
                
                // 直接调用目录接口获取章节列表
                String directoryUrl = "http://localhost:9999/api/fqsearch/directory/" + bookId;
                log.info("调用目录接口获取章节列表 - URL: {}", directoryUrl);
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(directoryUrl))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    log.error("目录接口调用失败 - bookId: {}, status: {}, body: {}", bookId, response.statusCode(), response.body());
                    return new ArrayList<>();
                }
                
                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.get("code").asInt() != 0) {
                    log.error("目录接口返回错误 - bookId: {}, message: {}", bookId, jsonNode.get("message").asText());
                    return new ArrayList<>();
                }
                
                JsonNode dataNode = jsonNode.get("data");
                if (dataNode == null || !dataNode.has("item_data_list")) {
                    log.warn("目录数据为空 - bookId: {}", bookId);
                    return new ArrayList<>();
                }
                
                JsonNode itemDataList = dataNode.get("item_data_list");
                if (itemDataList == null || !itemDataList.isArray()) {
                    log.warn("章节列表为空 - bookId: {}", bookId);
                    return new ArrayList<>();
                }
                
                // 提取章节ID
                chapterIds = new ArrayList<>();
                for (JsonNode item : itemDataList) {
                    if (item.has("item_id")) {
                        String itemId = item.get("item_id").asText();
                        if (itemId != null && !itemId.isEmpty()) {
                            chapterIds.add(itemId);
                        }
                    }
                }
                
                log.info("成功获取章节列表 - bookId: {}, 章节数量: {}", bookId, chapterIds.size());
                
                // 保存到Redis
                redisService.saveChapterList(bookId, chapterIds);
                log.info("章节列表已保存到Redis - bookId: {}, 章节数量: {}", bookId, chapterIds.size());
                
                return chapterIds;
                
            } catch (Exception e) {
                log.error("获取书籍章节ID失败 - bookId: {}", bookId, e);
                return new ArrayList<>();
            }
        }, executorService);
    }
}
