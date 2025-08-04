package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.*;
import com.anjia.unidbgserver.service.FQSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * FQ书籍搜索和目录控制器
 * 提供书籍搜索、目录获取等API接口
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/fqsearch", produces = MediaType.APPLICATION_JSON_VALUE)
public class FQSearchController {

    @Autowired
    private FQSearchService fqSearchService;

    /**
     * 搜索书籍 (GET方式)
     *
     * @param query 搜索关键词
     * @param tabType 搜索类型
     * @param offset 分页偏移量
     * @param count 每页数量
     * @param searchId 搜索ID (可选，用于二次搜索)
     * @return 搜索结果
     */
    @GetMapping("/books")
    public CompletableFuture<FQNovelResponse<FQSearchResponse>> searchBooksGet(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") Integer tabType,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer count,
            @RequestParam(required = false) String searchId) {

        if (log.isDebugEnabled()) {
            log.debug("搜索书籍请求(GET) - query: {}, tabType: {}, offset: {}, count: {}, searchId: {}",
                query, tabType, offset, count, searchId);
        }

        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("搜索关键词不能为空")
            );
        }

        if (tabType == null) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("搜索类型tabType不能为空")
            );
        }

        // 构建搜索请求
        FQSearchRequest searchRequest = new FQSearchRequest();
        searchRequest.setQuery(query.trim());
        searchRequest.setOffset(offset);
        searchRequest.setCount(count);
        searchRequest.setTabType(tabType);
        searchRequest.setSearchId(searchId);

        // 设置passback与offset相同
        searchRequest.setPassback(offset);

        // 使用增强搜索方法 - 实现两阶段搜索逻辑
        return fqSearchService.searchBooksEnhanced(searchRequest);
    }

    /**
     * 搜索书籍 (POST方式)
     *
     * @param searchRequest 搜索请求参数
     * @return 搜索结果
     */
    @PostMapping("/books")
    public CompletableFuture<FQNovelResponse<FQSearchResponse>> searchBooksPost(
            @RequestBody FQSearchRequest searchRequest) {

        if (log.isDebugEnabled()) {
            log.debug("搜索书籍请求(POST) - query: {}, tabType: {}, searchId: {}",
                searchRequest.getQuery(), searchRequest.getTabType(), searchRequest.getSearchId());
        }

        if (searchRequest.getQuery() == null || searchRequest.getQuery().trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("搜索关键词不能为空")
            );
        }

        if (searchRequest.getTabType() == null) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("搜索类型tabType不能为空")
            );
        }

        // 设置passback与offset相同
        if (searchRequest.getPassback() == null) {
            searchRequest.setPassback(searchRequest.getOffset());
        }

        // 使用增强搜索方法 - 实现两阶段搜索逻辑
        return fqSearchService.searchBooksEnhanced(searchRequest);
    }

    /**
     * 获取书籍目录 (GET方式)
     *
     * @param bookId 书籍ID
     * @return 书籍目录
     */
    @GetMapping("/directory/{bookId}")
    public CompletableFuture<FQNovelResponse<FQDirectoryResponse>> getBookDirectoryGet(
            @PathVariable String bookId) {

        if (log.isDebugEnabled()) {
            log.debug("获取书籍目录请求(GET) - bookId: {}", bookId);
        }

        if (bookId == null || bookId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }

        // 构建目录请求
        FQDirectoryRequest directoryRequest = new FQDirectoryRequest();
        directoryRequest.setBookId(bookId.trim());

        return fqSearchService.getBookDirectory(directoryRequest);
    }

    /**
     * 获取书籍目录 (POST方式)
     *
     * @param directoryRequest 目录请求参数
     * @return 书籍目录
     */
    @PostMapping("/directory")
    public CompletableFuture<FQNovelResponse<FQDirectoryResponse>> getBookDirectoryPost(
            @RequestBody FQDirectoryRequest directoryRequest) {

        if (log.isDebugEnabled()) {
            log.debug("获取书籍目录请求(POST) - bookId: {}", directoryRequest.getBookId());
        }

        if (directoryRequest.getBookId() == null || directoryRequest.getBookId().trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }

        return fqSearchService.getBookDirectory(directoryRequest);
    }

    /**
     * 快速搜索 (简化的搜索接口，只需要关键词)
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/quick")
    public CompletableFuture<FQNovelResponse<FQSearchResponse>> quickSearch(
            @RequestParam String query) {

        if (log.isDebugEnabled()) {
            log.debug("快速搜索请求 - query: {}", query);
        }

        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("搜索关键词不能为空")
            );
        }

        // 使用默认参数进行搜索
        FQSearchRequest searchRequest = new FQSearchRequest();
        searchRequest.setQuery(query.trim());
        searchRequest.setOffset(0);
        searchRequest.setCount(10); // 快速搜索返回较少结果

        return fqSearchService.searchBooks(searchRequest);
    }

    /**
     * 获取书籍章节列表 (简化接口，只返回章节ID和标题)
     *
     * @param bookId 书籍ID
     * @return 简化的章节列表
     */
    @GetMapping("/chapters/{bookId}")
    public CompletableFuture<FQNovelResponse<FQDirectoryResponse>> getBookChapters(
            @PathVariable String bookId) {

        if (log.isDebugEnabled()) {
            log.debug("获取书籍章节列表请求 - bookId: {}", bookId);
        }

        if (bookId == null || bookId.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                FQNovelResponse.error("书籍ID不能为空")
            );
        }

        // 构建目录请求
        FQDirectoryRequest directoryRequest = new FQDirectoryRequest();
        directoryRequest.setBookId(bookId.trim());
        directoryRequest.setNeedVersion(false); // 简化请求不需要版本信息

        return fqSearchService.getBookDirectory(directoryRequest);
    }
}
