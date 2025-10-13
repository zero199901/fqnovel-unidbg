package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 设备注册请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegisterRequest {
    
    /**
     * 设备品牌
     */
    private String deviceBrand;
    
    /**
     * 设备型号
     */
    private String deviceType;
    
    /**
     * 是否使用真实算法生成OpenUDID
     */
    @Builder.Default
    private Boolean useRealAlgorithm = true;
    
    /**
     * 是否使用真实品牌型号
     */
    @Builder.Default
    private Boolean useRealBrand = true;
    
    /**
     * 是否自动更新配置
     */
    @Builder.Default
    private Boolean autoUpdateConfig = true;
    
    /**
     * 是否自动重启项目
     */
    @Builder.Default
    private Boolean autoRestart = true;
}
