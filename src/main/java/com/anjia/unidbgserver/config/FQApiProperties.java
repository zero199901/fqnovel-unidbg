package com.anjia.unidbgserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FQ API 配置属性
 * 用于管理FQ API的设备参数和请求配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "fq.api")
public class FQApiProperties {
    
    /**
     * API基础URL
     */
    private String baseUrl = "https://api5-normal-sinfonlineb.fqnovel.com";
    
    /**
     * 默认User-Agent
     */
    private String userAgent = "com.dragon.read.oversea.gp/68132 (Linux; U; Android 10; zh_CN; OnePlus11; Build/V291IR;tt-ok/3.12.13.4-tiktok)";
    
    /**
     * Cookie配置
     */
    private String cookie = "store-region=cn-zj; store-region-src=did; install_id=933935730456617";
    
    /**
     * 设备参数配置
     */
    private Device device = new Device();
    
    @Data
    public static class Device {
        /**
         * 设备唯一标识符
         */
        private String cdid = "17f05006-423a-4172-be4b-7d26a42f2f4a";
        
        /**
         * 安装ID
         */
        private String installId = "933935730456617";
        
        /**
         * 设备ID
         */
        private String deviceId = "933935730452521";
        
        /**
         * 应用ID
         */
        private String aid = "1967";
        
        /**
         * 版本代码
         */
        private String versionCode = "68132";
        
        /**
         * 版本名称
         */
        private String versionName = "6.8.1.32";
        
        /**
         * 更新版本代码
         */
        private String updateVersionCode = "68132";
        
        /**
         * 设备类型
         */
        private String deviceType = "OnePlus11";
        
        /**
         * 设备品牌
         */
        private String deviceBrand = "OnePlus";
        
        /**
         * ROM版本
         */
        private String romVersion = "V291IR+release-keys";
        
        /**
         * 分辨率
         */
        private String resolution = "3200*1440";
        
        /**
         * DPI
         */
        private String dpi = "640";
        
        /**
         * 主机ABI
         */
        private String hostAbi = "arm64-v8a";
    }
}