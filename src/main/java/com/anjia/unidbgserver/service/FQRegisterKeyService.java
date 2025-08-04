package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.FQApiProperties;
import com.anjia.unidbgserver.dto.*;
import com.anjia.unidbgserver.utils.FQApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FQNovel RegisterKey缓存服务
 * 在启动时获取registerkey并缓存，支持keyver比较和自动刷新
 */
@Slf4j
@Service
public class FQRegisterKeyService {

    @Resource(name = "fqEncryptWorker")
    private FQEncryptServiceWorker fqEncryptServiceWorker;

    @Resource
    private FQApiProperties fqApiProperties;

    @Resource
    private FQApiUtils fqApiUtils;

    private final RestTemplate restTemplate = new RestTemplate();

    // 默认FQ变量配置
    private FqVariable defaultFqVariable;

    // 缓存的registerkey响应，按keyver分组
    private final Map<Long, FqRegisterKeyResponse> cachedRegisterKeys = new ConcurrentHashMap<>();

    // 当前默认的registerkey响应
    private volatile FqRegisterKeyResponse currentRegisterKey;

    /**
     * 获取默认FQ变量（延迟初始化）
     */
    private FqVariable getDefaultFqVariable() {
        if (defaultFqVariable == null) {
            defaultFqVariable = new FqVariable(fqApiProperties);
        }
        return defaultFqVariable;
    }

    /**
     * 启动时初始化registerkey
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化FQRegisterKeyService，获取初始registerkey...");
        try {
            // 获取初始registerkey
            FqRegisterKeyResponse response = fetchRegisterKey();
            if (response != null && response.getData() != null) {
                long keyver = response.getData().getKeyver();
                cachedRegisterKeys.put(keyver, response);
                currentRegisterKey = response;
                log.debug("初始registerkey获取成功，keyver: {}, content: {}", keyver,response.getData().getKey());
            } else {
                log.error("初始registerkey获取失败，响应为空");
            }
        } catch (Exception e) {
            log.error("初始化registerkey失败", e);
        }
    }

    /**
     * 获取registerkey，支持keyver比较和自动刷新
     *
     * @param requiredKeyver 需要的keyver，如果为null则使用当前缓存的key
     * @return RegisterKey响应
     */
    public synchronized FqRegisterKeyResponse getRegisterKey(Long requiredKeyver) throws Exception {
        // 如果没有指定keyver，返回当前缓存的key
        if (requiredKeyver == null) {
            if (currentRegisterKey != null) {
                return currentRegisterKey;
            }
            // 如果当前没有缓存的key，获取一个新的
            return refreshRegisterKey();
        }

        // 检查是否已经缓存了指定keyver的key
        FqRegisterKeyResponse cached = cachedRegisterKeys.get(requiredKeyver);
        if (cached != null) {
            log.debug("使用缓存的registerkey，keyver: {}", requiredKeyver);
            return cached;
        }

        // 如果当前缓存的key的keyver不匹配，需要刷新
        if (currentRegisterKey == null || currentRegisterKey.getData().getKeyver() != requiredKeyver) {
            log.info("当前registerkey keyver ({}) 与需要的keyver ({}) 不匹配，刷新registerkey...",
                    currentRegisterKey != null ? currentRegisterKey.getData().getKeyver() : "null",
                    requiredKeyver);
            return refreshRegisterKey();
        }

        return currentRegisterKey;
    }

    /**
     * 刷新registerkey
     *
     * @return 新的RegisterKey响应
     */
    public synchronized FqRegisterKeyResponse refreshRegisterKey() throws Exception {
        log.info("刷新registerkey...");
        FqRegisterKeyResponse response = fetchRegisterKey();

        if (response != null && response.getData() != null) {
            long keyver = response.getData().getKeyver();
            cachedRegisterKeys.put(keyver, response);
            currentRegisterKey = response;
            log.info("registerkey刷新成功，新keyver: {}", keyver);
            return response;
        } else {
            throw new Exception("刷新registerkey失败，响应为空");
        }
    }

    /**
     * 实际获取registerkey的方法
     *
     * @return RegisterKey响应
     */
    private FqRegisterKeyResponse fetchRegisterKey() throws Exception {
        FqVariable var = getDefaultFqVariable();

        // 使用工具类构建URL和参数
        String url = fqApiUtils.getBaseUrl() + "/reading/crypt/registerkey";
        Map<String, String> params = fqApiUtils.buildCommonApiParams(var);
        String fullUrl = fqApiUtils.buildUrlWithParams(url, params);

        // 生成统一的时间戳
        long currentTime = System.currentTimeMillis();

        // 使用工具类构建请求头
        Map<String, String> headers = fqApiUtils.buildRegisterKeyHeaders(currentTime);

        // 使用现有的签名服务生成签名
        Map<String, String> signedHeaders = fqEncryptServiceWorker.generateSignatureHeaders(fullUrl, headers).get();

        // 发起API请求
        HttpHeaders httpHeaders = new HttpHeaders();
        signedHeaders.forEach(httpHeaders::set);
        headers.forEach(httpHeaders::set);

        // 创建请求载荷
        FqRegisterKeyPayload payload = new FqRegisterKeyPayload(var);
        HttpEntity<FqRegisterKeyPayload> entity = new HttpEntity<>(payload, httpHeaders);

        log.debug("发送registerkey请求到: {}", fullUrl);
        log.debug("请求时间戳: {}", currentTime);
        log.debug("签名请求头: {}", httpHeaders);
        log.debug("请求载荷: content={}, keyver={}", payload.getContent(), payload.getKeyver());

        ResponseEntity<FqRegisterKeyResponse> response = restTemplate.exchange(
            fullUrl, HttpMethod.POST, entity, FqRegisterKeyResponse.class);

        log.debug("registerkey请求响应: code={}, message={}, keyver={}",
            response.getBody().getCode(), response.getBody().getMessage(),
            response.getBody().getData() != null ? response.getBody().getData().getKeyver() : "null");

        return response.getBody();
    }

    /**
     * 获取指定keyver的解密密钥
     *
     * @param requiredKeyver 需要的keyver
     * @return 解密密钥（十六进制字符串）
     */
    public String getDecryptionKey(Long requiredKeyver) throws Exception {
        FqRegisterKeyResponse registerKeyResponse = getRegisterKey(requiredKeyver);
        return registerKeyResponse.getData().getKey();
    }

    /**
     * 获取当前的解密密钥
     *
     * @return 解密密钥（十六进制字符串）
     */
    public String getCurrentDecryptionKey() throws Exception {
        return getDecryptionKey(null);
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        cachedRegisterKeys.clear();
        currentRegisterKey = null;
        log.info("registerkey缓存已清除");
    }

    /**
     * 获取缓存状态信息
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cachedKeyversCount", cachedRegisterKeys.size());
        status.put("cachedKeyvers", cachedRegisterKeys.keySet());
        status.put("currentKeyver", currentRegisterKey != null ? currentRegisterKey.getData().getKeyver() : null);
        return status;
    }
}
