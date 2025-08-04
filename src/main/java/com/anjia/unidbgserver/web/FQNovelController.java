package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.FQNovelBookInfo;
import com.anjia.unidbgserver.dto.FQNovelChapterInfo;
import com.anjia.unidbgserver.dto.FQNovelRequest;
import com.anjia.unidbgserver.dto.FQNovelResponse;
import com.anjia.unidbgserver.dto.FQBatchChapterRequest;
import com.anjia.unidbgserver.dto.FQBatchChapterResponse;
import com.anjia.unidbgserver.service.FQNovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * FQNovel API 控制器
 * 提供小说书籍和章节内容获取接口
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/fqnovel", produces = MediaType.APPLICATION_JSON_VALUE)
public class FQNovelController {

    @Autowired
    private FQNovelService fqNovelService;

    /**
     * 获取书籍信息
     * 
     * @param bookId 书籍ID
     * @return 书籍详情信息
     */
    @GetMapping("/book/{bookId}")
    public CompletableFuture<FQNovelResponse<FQNovelBookInfo>> getBookInfo(@PathVariable String bookId) {
        if (log.isDebugEnabled()) {
            log.debug("获取书籍信息请求 - bookId: {}", bookId);
        }
        
        if (bookId == null || bookId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }
        
        return fqNovelService.getBookInfo(bookId.trim());
    }

    /**
     * 获取章节内容 (GET方式，通过路径参数)
     * 
     * @param bookId 书籍ID
     * @param chapterId 章节ID
     * @param deviceId 设备ID (可选)
     * @param iid 应用ID (可选)
     * @param token 用户token (可选)
     * @return 章节内容信息
     */
    @GetMapping("/chapter/{bookId}/{chapterId}")
    public CompletableFuture<FQNovelResponse<FQNovelChapterInfo>> getChapterContent(
            @PathVariable String bookId,
            @PathVariable String chapterId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String iid,
            @RequestParam(required = false) String token,
            HttpServletRequest httpRequest) {
        
        if (log.isDebugEnabled()) {
            log.debug("获取章节内容请求 - bookId: {}, chapterId: {}", bookId, chapterId);
        }
        
        if (bookId == null || bookId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }
        
        if (chapterId == null || chapterId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("章节ID不能为空")
            );
        }
        
        // 构建请求对象
        FQNovelRequest request = new FQNovelRequest();
        request.setBookId(bookId.trim());
        request.setChapterId(chapterId.trim());
        request.setDeviceId(deviceId);
        request.setIid(iid);
        request.setToken(token);
        
        // 提取额外的请求头
        Map<String, String> extraHeaders = extractExtraHeaders(httpRequest);
        request.setExtraHeaders(extraHeaders);
        
        return fqNovelService.getChapterContent(request);
    }

    /**
     * 获取章节内容 (POST方式，通过请求体)
     * 
     * @param request 包含书籍ID、章节ID等信息的请求对象
     * @return 章节内容信息
     */
    @PostMapping("/chapter")
    public CompletableFuture<FQNovelResponse<FQNovelChapterInfo>> getChapterContentPost(
            @RequestBody FQNovelRequest request) {
        
        if (log.isDebugEnabled()) {
            log.debug("获取章节内容请求(POST) - bookId: {}, chapterId: {}", 
                request.getBookId(), request.getChapterId());
        }
        
        if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }
        
        if (request.getChapterId() == null || request.getChapterId().trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("章节ID不能为空")
            );
        }
        
        return fqNovelService.getChapterContent(request);
    }



    /**
     * 批量获取章节内容 (新功能)
     * 
     * @param request 批量章节请求
     * @return 批量章节响应，以章节ID为键，包含原始内容、纯文本内容、章节名和字数
     */
    @PostMapping("/chapters/batch")
    public CompletableFuture<FQNovelResponse<FQBatchChapterResponse>> getBatchChapterContent(
            @RequestBody FQBatchChapterRequest request) {
        
        if (log.isDebugEnabled()) {
            log.debug("批量获取章节内容请求 - bookId: {}, range: {}", 
                request.getBookId(), request.getChapterRange());
        }
        
        return fqNovelService.getBatchChapterContent(request);
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "FQNovel Service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        return healthStatus;
    }

    /**
     * 从HTTP请求中提取额外的请求头
     * 过滤掉标准请求头，只保留自定义头
     */
    private Map<String, String> extractExtraHeaders(HttpServletRequest request) {
        Map<String, String> extraHeaders = new HashMap<>();
        
        // 这里可以根据需要提取特定的请求头
        // 例如用户自定义的认证头、跟踪头等
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // 过滤掉标准HTTP头，只保留自定义头
            if (isCustomHeader(headerName)) {
                extraHeaders.put(headerName, headerValue);
            }
        }
        
        return extraHeaders;
    }

    /**
     * 判断是否为自定义请求头
     */
    private boolean isCustomHeader(String headerName) {
        if (headerName == null) {
            return false;
        }
        
        String lowerCaseName = headerName.toLowerCase();
        
        // 排除标准HTTP头
        return !lowerCaseName.equals("host") &&
               !lowerCaseName.equals("connection") &&
               !lowerCaseName.equals("content-length") &&
               !lowerCaseName.equals("content-type") &&
               !lowerCaseName.equals("accept") &&
               !lowerCaseName.equals("accept-encoding") &&
               !lowerCaseName.equals("accept-language") &&
               !lowerCaseName.equals("user-agent") &&
               !lowerCaseName.startsWith("sec-") &&
               // 包含自定义头，如以x-开头的头
               (lowerCaseName.startsWith("x-") || lowerCaseName.startsWith("fq-"));
    }
}