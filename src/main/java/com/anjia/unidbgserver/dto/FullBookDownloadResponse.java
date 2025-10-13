package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 全本下载响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullBookDownloadResponse {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 书籍信息
     */
    private FQNovelBookInfo bookInfo;
    
    /**
     * 总章节数
     */
    private Integer totalChapters;
    
    /**
     * 已下载章节数
     */
    private Integer downloadedChapters;
    
    /**
     * 当前批次章节数
     */
    private Integer currentBatchSize;
    
    /**
     * 当前批次索引
     */
    private Integer currentBatchIndex;
    
    /**
     * 总批次数
     */
    private Integer totalBatches;
    
    /**
     * 当前批次的章节信息
     */
    private Map<String, FQNovelChapterInfo> chapters;
    
    /**
     * 章节ID列表
     */
    private List<String> chapterIds;
    
    /**
     * 是否完成
     */
    private Boolean completed;
    
    /**
     * 进度百分比
     */
    private Double progress;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 创建进度响应
     */
    public static FullBookDownloadResponse progress(String bookId, FQNovelBookInfo bookInfo, 
                                                   Integer totalChapters, Integer downloadedChapters,
                                                   Integer currentBatchSize, Integer currentBatchIndex,
                                                   Integer totalBatches, Map<String, FQNovelChapterInfo> chapters,
                                                   List<String> chapterIds, String message) {
        double progress = totalChapters > 0 ? (double) downloadedChapters / totalChapters * 100 : 0;
        boolean completed = downloadedChapters >= totalChapters;
        
        return FullBookDownloadResponse.builder()
            .bookId(bookId)
            .bookInfo(bookInfo)
            .totalChapters(totalChapters)
            .downloadedChapters(downloadedChapters)
            .currentBatchSize(currentBatchSize)
            .currentBatchIndex(currentBatchIndex)
            .totalBatches(totalBatches)
            .chapters(chapters)
            .chapterIds(chapterIds)
            .completed(completed)
            .progress(progress)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建完成响应
     */
    public static FullBookDownloadResponse completed(String bookId, FQNovelBookInfo bookInfo, 
                                                     Integer totalChapters, String message) {
        return FullBookDownloadResponse.builder()
            .bookId(bookId)
            .bookInfo(bookInfo)
            .totalChapters(totalChapters)
            .downloadedChapters(totalChapters)
            .completed(true)
            .progress(100.0)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
