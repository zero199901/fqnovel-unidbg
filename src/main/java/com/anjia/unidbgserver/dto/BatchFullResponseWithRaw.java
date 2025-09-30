package com.anjia.unidbgserver.dto;

import lombok.Data;

import java.util.Map;

/**
 * 包含原始响应的批量获取响应
 */
@Data
public class BatchFullResponseWithRaw {
    
    /**
     * 批量获取响应数据
     */
    private FqIBatchFullResponse batchResponse;
    
    /**
     * 原始API响应信息
     */
    private FQBatchChapterResponse.RawApiResponse rawApiResponse;
    
    /**
     * 构造函数
     */
    public BatchFullResponseWithRaw(FqIBatchFullResponse batchResponse, FQBatchChapterResponse.RawApiResponse rawApiResponse) {
        this.batchResponse = batchResponse;
        this.rawApiResponse = rawApiResponse;
    }
}
