package com.anjia.unidbgserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 自动恢复任务服务
 * 定时检查未完成的下载任务并自动恢复
 */
@Slf4j
@Service
public class AutoResumeTaskService {

    @Autowired
    private FullBookDownloadService fullBookDownloadService;

    /**
     * 定时检查未完成的任务（每30分钟执行一次）
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void scheduledAutoResume() {
        log.info("开始定时检查未完成的下载任务");
        
        CompletableFuture.runAsync(() -> {
            try {
                FullBookDownloadService.AutoResumeAllResult result = 
                    fullBookDownloadService.autoResumeAllDownloads().get();
                
                if (result.isSuccess()) {
                    log.info("定时自动恢复任务完成 - {}", result.getMessage());
                } else {
                    log.error("定时自动恢复任务失败 - {}", result.getMessage());
                }
                
            } catch (Exception e) {
                log.error("定时自动恢复任务执行失败", e);
            }
        });
    }

    /**
     * 手动触发自动恢复检查
     */
    public CompletableFuture<FullBookDownloadService.AutoResumeAllResult> triggerAutoResume() {
        log.info("手动触发自动恢复检查");
        return fullBookDownloadService.autoResumeAllDownloads();
    }
}
