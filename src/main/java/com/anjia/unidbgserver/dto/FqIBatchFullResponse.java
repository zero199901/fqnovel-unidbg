package com.anjia.unidbgserver.dto;

import com.anjia.unidbgserver.service.FqCrypto;
import com.anjia.unidbgserver.service.FQRegisterKeyService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FQNovel 批量获取响应
 * 对应 Rust 中的 FqIBatchFullResponse 结构
 */
@Slf4j
@Data
public class FqIBatchFullResponse {
    
    /**
     * 响应码
     */
    private long code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据 - 章节ID到内容的映射
     */
    private Map<String, ItemContent> data;
    
    /**
     * 获取解密的内容
     * 
     * @param registerKeyService RegisterKey缓存服务
     * @return 章节ID和解密内容的列表
     */
    public List<Map.Entry<String, String>> getDecryptContents(
            FQRegisterKeyService registerKeyService) throws Exception {
        
        List<Map.Entry<String, String>> results = new ArrayList<>();
        
        for (Map.Entry<String, ItemContent> entry : this.data.entrySet()) {
            String itemId = entry.getKey();
            ItemContent content = entry.getValue();
            
            try {
                // 获取章节的keyVersion
                Long contentKeyver = content.getKeyVersion();
                log.debug("章节 {} 的keyVersion: {}", itemId, contentKeyver);
                
                // 获取对应keyver的解密密钥
                String key = registerKeyService.getDecryptionKey(contentKeyver);
                
                // 使用新的解密和解压缩方法
                String decryptedContent = FqCrypto.decryptAndDecompressContent(content.getContent(), key);
                results.add(new java.util.AbstractMap.SimpleEntry<>(itemId, decryptedContent));
                
                log.debug("章节 {} 解密成功，内容长度: {}", itemId, decryptedContent.length());
                
            } catch (Exception e) {
                log.error("解密章节内容失败 - itemId: {}, keyVersion: {}", itemId, content.getKeyVersion(), e);
                // 继续处理其他章节
            }
        }
        
        return results;
    }
    
}