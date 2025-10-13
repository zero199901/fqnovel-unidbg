package com.anjia.unidbgserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 设备管理操作结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceManagementResult {
    
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
     * 创建成功结果
     */
    public static DeviceManagementResult success(String message, DeviceInfo deviceInfo) {
        return DeviceManagementResult.builder()
            .success(true)
            .message(message)
            .deviceInfo(deviceInfo)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建失败结果
     */
    public static DeviceManagementResult error(String message) {
        return DeviceManagementResult.builder()
            .success(false)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建失败结果（带错误代码）
     */
    public static DeviceManagementResult error(String message, String errorCode) {
        return DeviceManagementResult.builder()
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
