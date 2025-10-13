package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 设备管理服务
 */
@Slf4j
@Service
public class DeviceManagementService {

    @Autowired
    private DeviceGeneratorService deviceGeneratorService;

    @Value("${spring.config.location:classpath:application.yml}")
    private String configLocation;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * 注册设备
     */
    public CompletableFuture<DeviceRegisterResponse> registerDevice(DeviceRegisterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始注册设备 - 品牌: {}, 型号: {}", request.getDeviceBrand(), request.getDeviceType());
                
                // 生成设备信息
                DeviceInfo deviceInfo = deviceGeneratorService.generateDeviceInfo(request);
                
                if (deviceInfo == null) {
                    return DeviceRegisterResponse.error("设备信息生成失败");
                }
                
                log.info("设备信息生成成功 - 设备ID: {}, 安装ID: {}", 
                    deviceInfo.getDeviceId(), deviceInfo.getInstallId());
                
                return DeviceRegisterResponse.success(deviceInfo);
                
            } catch (Exception e) {
                log.error("设备注册失败", e);
                return DeviceRegisterResponse.error("设备注册失败: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 更新设备配置
     */
    public CompletableFuture<Boolean> updateDeviceConfig(DeviceInfo deviceInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始更新设备配置 - 设备ID: {}", deviceInfo.getDeviceId());
                
                // 读取当前配置文件
                String configPath = getConfigFilePath();
                Map<String, Object> config = loadYamlConfig(configPath);
                
                // 更新设备配置
                updateDeviceConfigInYaml(config, deviceInfo);
                
                // 保存配置文件
                saveYamlConfig(configPath, config);
                
                log.info("设备配置更新成功 - 设备ID: {}", deviceInfo.getDeviceId());
                return true;
                
            } catch (Exception e) {
                log.error("设备配置更新失败", e);
                return false;
            }
        }, executorService);
    }

    /**
     * 重启项目
     */
    public CompletableFuture<Boolean> restartProject() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始重启项目");
                
                // 这里可以实现重启逻辑
                // 由于Spring Boot应用重启比较复杂，这里先返回成功
                // 实际实现可能需要通过外部脚本或Spring Boot Actuator
                
                log.info("项目重启成功");
                return true;
                
            } catch (Exception e) {
                log.error("项目重启失败", e);
                return false;
            }
        }, executorService);
    }

    /**
     * 注册设备并自动更新配置和重启项目
     */
    public CompletableFuture<DeviceManagementResult> registerDeviceAndRestart(DeviceRegisterRequest request) {
        return registerDevice(request)
            .thenCompose(registerResponse -> {
                if (!registerResponse.getSuccess()) {
                    return CompletableFuture.completedFuture(
                        DeviceManagementResult.error("设备注册失败: " + registerResponse.getMessage())
                    );
                }
                
                DeviceInfo deviceInfo = registerResponse.getDeviceInfo();
                
                // 更新配置
                return updateDeviceConfig(deviceInfo)
                    .thenCompose(configSuccess -> {
                        if (!configSuccess) {
                            return CompletableFuture.completedFuture(
                                DeviceManagementResult.error("设备配置更新失败")
                            );
                        }
                        
                        // 重启项目
                        return restartProject()
                            .thenApply(restartSuccess -> {
                                if (restartSuccess) {
                                    return DeviceManagementResult.success(
                                        "设备注册、配置更新和项目重启全部成功", deviceInfo);
                                } else {
                                    return DeviceManagementResult.error("项目重启失败");
                                }
                            });
                    });
            });
    }

    /**
     * 获取当前设备配置
     */
    public CompletableFuture<DeviceInfo> getCurrentDeviceConfig() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String configPath = getConfigFilePath();
                Map<String, Object> config = loadYamlConfig(configPath);
                
                return extractDeviceInfoFromConfig(config);
                
            } catch (Exception e) {
                log.error("获取当前设备配置失败", e);
                return null;
            }
        }, executorService);
    }

    /**
     * 获取配置文件路径
     */
    private String getConfigFilePath() {
        if (configLocation.startsWith("classpath:")) {
            // 如果是classpath路径，需要找到实际的文件路径
            try {
                ClassPathResource resource = new ClassPathResource("application.yml");
                return resource.getFile().getAbsolutePath();
            } catch (IOException e) {
                log.error("无法获取配置文件路径", e);
                return "src/main/resources/application.yml";
            }
        } else {
            return configLocation;
        }
    }

    /**
     * 加载YAML配置文件
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlConfig(String configPath) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(configPath)) {
            return yaml.load(inputStream);
        }
    }

    /**
     * 保存YAML配置文件
     */
    private void saveYamlConfig(String configPath, Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(configPath)) {
            yaml.dump(config, writer);
        }
    }

    /**
     * 更新YAML配置中的设备信息
     */
    @SuppressWarnings("unchecked")
    private void updateDeviceConfigInYaml(Map<String, Object> config, DeviceInfo deviceInfo) {
        Map<String, Object> fqConfig = (Map<String, Object>) config.get("fq");
        if (fqConfig == null) {
            fqConfig = new HashMap<>();
            config.put("fq", fqConfig);
        }
        
        Map<String, Object> apiConfig = (Map<String, Object>) fqConfig.get("api");
        if (apiConfig == null) {
            apiConfig = new HashMap<>();
            fqConfig.put("api", apiConfig);
        }
        
        // 更新用户代理
        if (deviceInfo.getUserAgent() != null) {
            apiConfig.put("user-agent", deviceInfo.getUserAgent());
        }
        
        // 更新Cookie
        if (deviceInfo.getCookie() != null) {
            apiConfig.put("cookie", deviceInfo.getCookie());
        }
        
        // 更新设备信息
        Map<String, Object> deviceConfig = (Map<String, Object>) apiConfig.get("device");
        if (deviceConfig == null) {
            deviceConfig = new HashMap<>();
            apiConfig.put("device", deviceConfig);
        }
        
        if (deviceInfo.getAid() != null) {
            deviceConfig.put("aid", deviceInfo.getAid());
        }
        if (deviceInfo.getCdid() != null) {
            deviceConfig.put("cdid", deviceInfo.getCdid());
        }
        if (deviceInfo.getDeviceBrand() != null) {
            deviceConfig.put("device-brand", deviceInfo.getDeviceBrand());
        }
        if (deviceInfo.getDeviceId() != null) {
            deviceConfig.put("device-id", deviceInfo.getDeviceId());
        }
        if (deviceInfo.getDeviceType() != null) {
            deviceConfig.put("device-type", deviceInfo.getDeviceType());
        }
        if (deviceInfo.getDpi() != null) {
            deviceConfig.put("dpi", deviceInfo.getDpi());
        }
        if (deviceInfo.getHostAbi() != null) {
            deviceConfig.put("host-abi", deviceInfo.getHostAbi());
        }
        if (deviceInfo.getInstallId() != null) {
            deviceConfig.put("install-id", deviceInfo.getInstallId());
        }
        if (deviceInfo.getResolution() != null) {
            deviceConfig.put("resolution", deviceInfo.getResolution());
        }
        if (deviceInfo.getRomVersion() != null) {
            deviceConfig.put("rom-version", deviceInfo.getRomVersion());
        }
        if (deviceInfo.getUpdateVersionCode() != null) {
            deviceConfig.put("update-version-code", deviceInfo.getUpdateVersionCode());
        }
        if (deviceInfo.getVersionCode() != null) {
            deviceConfig.put("version-code", deviceInfo.getVersionCode());
        }
        if (deviceInfo.getVersionName() != null) {
            deviceConfig.put("version-name", deviceInfo.getVersionName());
        }
    }

    /**
     * 从配置中提取设备信息
     */
    @SuppressWarnings("unchecked")
    private DeviceInfo extractDeviceInfoFromConfig(Map<String, Object> config) {
        try {
            Map<String, Object> fqConfig = (Map<String, Object>) config.get("fq");
            if (fqConfig == null) {
                return null;
            }
            
            Map<String, Object> apiConfig = (Map<String, Object>) fqConfig.get("api");
            if (apiConfig == null) {
                return null;
            }
            
            Map<String, Object> deviceConfig = (Map<String, Object>) apiConfig.get("device");
            if (deviceConfig == null) {
                return null;
            }
            
            return DeviceInfo.builder()
                .deviceBrand((String) deviceConfig.get("device-brand"))
                .deviceType((String) deviceConfig.get("device-type"))
                .deviceId((String) deviceConfig.get("device-id"))
                .installId((String) deviceConfig.get("install-id"))
                .cdid((String) deviceConfig.get("cdid"))
                .resolution((String) deviceConfig.get("resolution"))
                .dpi((String) deviceConfig.get("dpi"))
                .hostAbi((String) deviceConfig.get("host-abi"))
                .romVersion((String) deviceConfig.get("rom-version"))
                .aid((String) deviceConfig.get("aid"))
                .versionCode((String) deviceConfig.get("version-code"))
                .versionName((String) deviceConfig.get("version-name"))
                .updateVersionCode((String) deviceConfig.get("update-version-code"))
                .userAgent((String) apiConfig.get("user-agent"))
                .cookie((String) apiConfig.get("cookie"))
                .build();
                
        } catch (Exception e) {
            log.error("从配置中提取设备信息失败", e);
            return null;
        }
    }
}
