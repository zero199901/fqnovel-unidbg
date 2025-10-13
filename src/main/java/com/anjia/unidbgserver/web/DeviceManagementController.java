package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.DeviceInfo;
import com.anjia.unidbgserver.dto.DeviceRegisterRequest;
import com.anjia.unidbgserver.dto.DeviceRegisterResponse;
import com.anjia.unidbgserver.service.DeviceManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 设备管理控制器
 * 提供设备注册、配置更新、项目重启等功能
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/device", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceManagementController {

    @Autowired
    private DeviceManagementService deviceManagementService;

    /**
     * 注册新设备并更新配置
     * 
     * @param request 设备注册请求
     * @return 设备注册响应
     */
    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<DeviceRegisterResponse>> registerDevice(
            @RequestBody DeviceRegisterRequest request) {
        
        if (log.isDebugEnabled()) {
            log.debug("设备注册请求 - 品牌: {}, 型号: {}", 
                request.getDeviceBrand(), request.getDeviceType());
        }
        
        return deviceManagementService.registerDevice(request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 更新设备配置
     * 
     * @param deviceInfo 设备信息
     * @return 更新结果
     */
    @PostMapping("/update-config")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> updateDeviceConfig(
            @RequestBody DeviceInfo deviceInfo) {
        
        if (log.isDebugEnabled()) {
            log.debug("更新设备配置请求 - 设备ID: {}", deviceInfo.getDeviceId());
        }
        
        return deviceManagementService.updateDeviceConfig(deviceInfo)
            .thenApply(success -> {
                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "设备配置更新成功");
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "设备配置更新失败");
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 重启项目
     * 
     * @return 重启结果
     */
    @PostMapping("/restart")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> restartProject() {
        
        if (log.isDebugEnabled()) {
            log.debug("项目重启请求");
        }
        
        return deviceManagementService.restartProject()
            .thenApply(success -> {
                Map<String, Object> response = new HashMap<>();
                if (success) {
                    response.put("success", true);
                    response.put("message", "项目重启成功");
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "项目重启失败");
                    response.put("timestamp", System.currentTimeMillis());
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 注册设备并自动更新配置和重启项目
     * 
     * @param request 设备注册请求
     * @return 完整操作结果
     */
    @PostMapping("/register-and-restart")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> registerDeviceAndRestart(
            @RequestBody DeviceRegisterRequest request) {
        
        if (log.isDebugEnabled()) {
            log.debug("设备注册并重启请求 - 品牌: {}, 型号: {}", 
                request.getDeviceBrand(), request.getDeviceType());
        }
        
        return deviceManagementService.registerDeviceAndRestart(request)
            .thenApply(result -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("deviceInfo", result.getDeviceInfo());
                response.put("timestamp", System.currentTimeMillis());
                
                if (result.isSuccess()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body(response);
                }
            });
    }

    /**
     * 获取当前设备配置
     * 
     * @return 当前设备配置信息
     */
    @GetMapping("/current-config")
    public CompletableFuture<ResponseEntity<DeviceInfo>> getCurrentDeviceConfig() {
        
        if (log.isDebugEnabled()) {
            log.debug("获取当前设备配置请求");
        }
        
        return deviceManagementService.getCurrentDeviceConfig()
            .thenApply(deviceInfo -> {
                if (deviceInfo != null) {
                    return ResponseEntity.ok(deviceInfo);
                } else {
                    return ResponseEntity.notFound().build();
                }
            });
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("service", "Device Management Service");
        healthStatus.put("timestamp", System.currentTimeMillis());
        return healthStatus;
    }
}
