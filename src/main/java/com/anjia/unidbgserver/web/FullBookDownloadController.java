package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.FQNovelChapterInfo;
import com.anjia.unidbgserver.dto.FullBookDownloadRequest;
import com.anjia.unidbgserver.dto.FullBookDownloadResponse;
import com.anjia.unidbgserver.service.AutoResumeTaskService;
import com.anjia.unidbgserver.service.FullBookDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 全本下载控制器
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/fullbook", produces = MediaType.APPLICATION_JSON_VALUE)
public class FullBookDownloadController {

    @Autowired
    private FullBookDownloadService fullBookDownloadService;

    @Autowired
    private AutoResumeTaskService autoResumeTaskService;

    /**
     * 全本下载（流式返回）
     * 
     * @param request 下载请求
     * @return 流式响应
     */
    @PostMapping("/download")
    public Flux<FullBookDownloadResponse> downloadFullBook(@RequestBody FullBookDownloadRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("全本下载请求 - bookId: {}, batchSize: {}", request.getBookId(), request.getBatchSize());
        }
        
        if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("书籍ID不能为空"));
        }
        
        return fullBookDownloadService.downloadFullBook(request);
    }

    /**
     * 全本下载（GET 简化版，支持默认参数）
     * 示例：GET /api/fullbook/download?bookId=1629221175143432
     */
    @GetMapping("/download")
    public Flux<FullBookDownloadResponse> downloadFullBookGet(
            @RequestParam("bookId") String bookId,
            @RequestParam(value = "batchSize", required = false, defaultValue = "30") int batchSize,
            @RequestParam(value = "maxChapters", required = false, defaultValue = "0") int maxChapters,
            @RequestParam(value = "saveToRedis", required = false, defaultValue = "true") boolean saveToRedis
    ) {
        FullBookDownloadRequest request = FullBookDownloadRequest.builder()
            .bookId(bookId)
            .batchSize(batchSize)
            .maxChapters(maxChapters)
            .saveToRedis(saveToRedis)
            .streamResponse(true)
            .build();
        return downloadFullBook(request);
    }

    /**
     * 全本下载（简化版，使用默认参数）
     * 
     * @param bookId 书籍ID
     * @return 流式响应
     */
    @PostMapping("/download/{bookId}")
    public Flux<FullBookDownloadResponse> downloadFullBookSimple(@PathVariable String bookId) {
        FullBookDownloadRequest request = FullBookDownloadRequest.builder()
            .bookId(bookId)
            .batchSize(30)
            .saveToRedis(true)
            .streamResponse(true)
            .build();
        
        return downloadFullBook(request);
    }

    /**
     * 获取已下载的章节列表
     * 
     * @param bookId 书籍ID
     * @return 章节列表
     */
    @GetMapping("/chapters/{bookId}")
    public CompletableFuture<ResponseEntity<List<FQNovelChapterInfo>>> getDownloadedChapters(@PathVariable String bookId) {
        if (log.isDebugEnabled()) {
            log.debug("获取已下载章节列表请求 - bookId: {}", bookId);
        }
        
        return fullBookDownloadService.getDownloadedChapters(bookId)
            .thenApply(chapters -> {
                if (chapters != null) {
                    return ResponseEntity.ok(chapters);
                } else {
                    return ResponseEntity.notFound().build();
                }
            });
    }

    /**
     * 获取下载进度
     * 
     * @param bookId 书籍ID
     * @return 下载进度信息
     */
    @GetMapping("/progress/{bookId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getDownloadProgress(@PathVariable String bookId) {
        if (log.isDebugEnabled()) {
            log.debug("获取下载进度请求 - bookId: {}", bookId);
        }
        
        return fullBookDownloadService.getDownloadProgress(bookId)
            .thenApply(progress -> {
                if (progress.containsKey("error")) {
                    return ResponseEntity.badRequest().body(progress);
                } else {
                    return ResponseEntity.ok(progress);
                }
            });
    }

    /**
     * 自动检查并恢复下载
     * 
     * @param bookId 书籍ID
     * @return 恢复下载结果
     */
    @PostMapping("/auto-resume/{bookId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> autoResumeDownload(@PathVariable String bookId) {
        if (log.isDebugEnabled()) {
            log.debug("自动恢复下载请求 - bookId: {}", bookId);
        }
        
        return fullBookDownloadService.autoResumeDownload(bookId)
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("bookId", bookId);
                response.put("timestamp", System.currentTimeMillis());
                
                if (result.isSuccess()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 检查所有未完成的任务并自动恢复下载
     * 
     * @return 批量恢复结果
     */
    @PostMapping("/auto-resume-all")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> autoResumeAllDownloads() {
        if (log.isDebugEnabled()) {
            log.debug("批量自动恢复下载请求");
        }
        
        return fullBookDownloadService.autoResumeAllDownloads()
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("processedBooks", result.getProcessedBooks());
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            });
    }

    /**
     * 手动触发定时自动恢复任务
     * 
     * @return 触发结果
     */
    @PostMapping("/trigger-auto-resume")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> triggerAutoResume() {
        if (log.isDebugEnabled()) {
            log.debug("手动触发自动恢复任务请求");
        }
        
        return autoResumeTaskService.triggerAutoResume()
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("processedBooks", result.getProcessedBooks());
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            });
    }

    /**
     * 检查所有书籍的下载状态（不执行恢复，仅检查）
     * 
     * @return 所有书籍的下载状态
     */
    @GetMapping("/check-all-status")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> checkAllDownloadStatus() {
        if (log.isDebugEnabled()) {
            log.debug("检查所有书籍下载状态请求");
        }
        
        return fullBookDownloadService.checkAllDownloadStatus()
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("books", result.getBooks());
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            });
    }

    /**
     * 删除已下载的章节
     * 
     * @param bookId 书籍ID
     * @return 删除结果
     */
    @DeleteMapping("/chapters/{bookId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteDownloadedChapters(@PathVariable String bookId) {
        if (log.isDebugEnabled()) {
            log.debug("删除已下载章节请求 - bookId: {}", bookId);
        }
        
        return fullBookDownloadService.deleteDownloadedChapters(bookId)
            .thenApply(success -> {
                Map<String, Object> response = Map.of(
                    "success", success,
                    "message", success ? "删除成功" : "删除失败",
                    "timestamp", System.currentTimeMillis()
                );
                
                if (success) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> healthStatus = Map.of(
            "status", "UP",
            "service", "Full Book Download Service",
            "timestamp", System.currentTimeMillis()
        );
        return healthStatus;
    }
}
