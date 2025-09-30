package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.DeviceRotationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 设备轮换管理控制器
 * 提供设备轮换状态查询、手动切换等功能
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/device-rotation", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceRotationController {

    @Resource
    private DeviceRotationService deviceRotationService;

    /**
     * 获取设备池信息
     * 
     * @return 设备池状态信息
     */
    @GetMapping("/info")
    public Map<String, Object> getDevicePoolInfo() {
        if (log.isDebugEnabled()) {
            log.debug("获取设备池信息请求");
        }
        
        return deviceRotationService.getDevicePoolInfo();
    }

    /**
     * 手动切换到指定设备
     * 
     * @param deviceIndex 设备索引
     * @return 切换结果
     */
    @PostMapping("/switch/{deviceIndex}")
    public Map<String, Object> switchToDevice(@PathVariable int deviceIndex) {
        if (log.isDebugEnabled()) {
            log.debug("手动切换设备请求 - deviceIndex: {}", deviceIndex);
        }
        
        Map<String, Object> result = deviceRotationService.getDevicePoolInfo();
        int totalDevices = (Integer) result.get("totalDevices");
        
        if (deviceIndex < 0 || deviceIndex >= totalDevices) {
            return Map.of(
                "success", false,
                "message", "无效的设备索引",
                "availableRange", "0-" + (totalDevices - 1),
                "totalDevices", totalDevices
            );
        }
        
        deviceRotationService.switchToDevice(deviceIndex);
        
        return Map.of(
            "success", true,
            "message", "设备切换成功",
            "deviceIndex", deviceIndex,
            "deviceInfo", result.get("devices")
        );
    }

    /**
     * 重置请求计数器
     * 
     * @return 重置结果
     */
    @PostMapping("/reset-counter")
    public Map<String, Object> resetRequestCounter() {
        if (log.isDebugEnabled()) {
            log.debug("重置请求计数器请求");
        }
        
        deviceRotationService.resetRequestCounter();
        
        return Map.of(
            "success", true,
            "message", "请求计数器已重置",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 获取当前设备信息
     * 
     * @return 当前设备详细信息
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentDevice() {
        if (log.isDebugEnabled()) {
            log.debug("获取当前设备信息请求");
        }
        
        Map<String, Object> poolInfo = deviceRotationService.getDevicePoolInfo();
        int currentIndex = (Integer) poolInfo.get("currentDeviceIndex");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> devices = (java.util.List<Map<String, Object>>) poolInfo.get("devices");
        
        Map<String, Object> currentDevice = devices.get(currentIndex);
        
        return Map.of(
            "currentDevice", currentDevice,
            "requestCount", poolInfo.get("requestCount"),
            "nextRotationAt", poolInfo.get("nextRotationAt"),
            "rotationThreshold", poolInfo.get("rotationThreshold")
        );
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> poolInfo = deviceRotationService.getDevicePoolInfo();
        
        return Map.of(
            "status", "UP",
            "service", "Device Rotation Service",
            "timestamp", System.currentTimeMillis(),
            "totalDevices", poolInfo.get("totalDevices"),
            "currentDeviceIndex", poolInfo.get("currentDeviceIndex"),
            "requestCount", poolInfo.get("requestCount")
        );
    }
}
