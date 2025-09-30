package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.FqRegisterKeyResponse;
import com.anjia.unidbgserver.service.FQRegisterKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * FQ RegisterKey 管理控制器
 * 提供registerkey的查询、刷新等功能
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/fq-registerkey", produces = MediaType.APPLICATION_JSON_VALUE)
public class FQRegisterKeyController {

    @Resource
    private FQRegisterKeyService registerKeyService;

    /**
     * 获取当前registerkey状态
     * 
     * @return registerkey状态信息
     */
    @GetMapping("/status")
    public Map<String, Object> getRegisterKeyStatus() {
        if (log.isDebugEnabled()) {
            log.debug("获取registerkey状态请求");
        }
        
        Map<String, Object> status = registerKeyService.getCacheStatus();
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }

    /**
     * 手动刷新registerkey
     * 
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshRegisterKey() {
        if (log.isDebugEnabled()) {
            log.debug("手动刷新registerkey请求");
        }
        
        try {
            FqRegisterKeyResponse response = registerKeyService.refreshRegisterKey();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "registerkey刷新成功");
            result.put("timestamp", System.currentTimeMillis());
            
            if (response != null && response.getData() != null) {
                result.put("keyver", response.getData().getKeyver());
                result.put("keyLength", response.getData().getKey() != null ? response.getData().getKey().length() : 0);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("手动刷新registerkey失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "registerkey刷新失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
        }
    }

    /**
     * 清除registerkey缓存
     * 
     * @return 清除结果
     */
    @PostMapping("/clear-cache")
    public Map<String, Object> clearRegisterKeyCache() {
        if (log.isDebugEnabled()) {
            log.debug("清除registerkey缓存请求");
        }
        
        registerKeyService.clearCache();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "registerkey缓存已清除");
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * 获取指定keyver的registerkey
     * 
     * @param keyver 需要的keyver
     * @return registerkey信息
     */
    @GetMapping("/get/{keyver}")
    public Map<String, Object> getRegisterKeyByKeyver(@PathVariable Long keyver) {
        if (log.isDebugEnabled()) {
            log.debug("获取指定keyver的registerkey请求 - keyver: {}", keyver);
        }
        
        try {
            FqRegisterKeyResponse response = registerKeyService.getRegisterKey(keyver);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "registerkey获取成功");
            result.put("timestamp", System.currentTimeMillis());
            
            if (response != null && response.getData() != null) {
                result.put("keyver", response.getData().getKeyver());
                result.put("keyLength", response.getData().getKey() != null ? response.getData().getKey().length() : 0);
                result.put("isCached", true);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取指定keyver的registerkey失败 - keyver: {}", keyver, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "registerkey获取失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
        }
    }
}
