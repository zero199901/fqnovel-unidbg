package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 设备信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInfo {
    
    /**
     * 设备品牌
     */
    private String deviceBrand;
    
    /**
     * 设备型号
     */
    private String deviceType;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 安装ID
     */
    private String installId;
    
    /**
     * CDID
     */
    private String cdid;
    
    /**
     * 分辨率
     */
    private String resolution;
    
    /**
     * DPI
     */
    private String dpi;
    
    /**
     * 主机ABI
     */
    private String hostAbi;
    
    /**
     * ROM版本
     */
    private String romVersion;
    
    /**
     * Android版本
     */
    private String osVersion;
    
    /**
     * Android API级别
     */
    private Integer osApi;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * Cookie
     */
    private String cookie;
    
    /**
     * 应用ID
     */
    private String aid;
    
    /**
     * 版本代码
     */
    private String versionCode;
    
    /**
     * 版本名称
     */
    private String versionName;
    
    /**
     * 更新版本代码
     */
    private String updateVersionCode;
}
