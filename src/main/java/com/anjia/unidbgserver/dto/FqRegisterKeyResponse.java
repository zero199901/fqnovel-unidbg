package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQNovel 注册密钥响应
 * 对应 Rust 中的 FqRegisterKeyResponse 结构
 */
@Data
public class FqRegisterKeyResponse {

    /**
     * 响应码
     */
    private long code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
//    private FqRegisterKeyPayload data;
    private FqRegisterKeyPayloadResponse data;
}
