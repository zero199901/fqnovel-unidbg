package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 设备注册响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegisterResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 创建成功响应
     */
    public static DeviceRegisterResponse success(DeviceInfo deviceInfo) {
        return DeviceRegisterResponse.builder()
            .success(true)
            .message("设备注册成功")
            .deviceInfo(deviceInfo)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建失败响应
     */
    public static DeviceRegisterResponse error(String message) {
        return DeviceRegisterResponse.builder()
            .success(false)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建失败响应（带错误代码）
     */
    public static DeviceRegisterResponse error(String message, String errorCode) {
        return DeviceRegisterResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 获取成功状态
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.success);
    }
}
