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
                log.info("开始更新设备配置 - 设备ID: {}, 品牌: {}, 型号: {}", 
                    deviceInfo.getDeviceId(), deviceInfo.getDeviceBrand(), deviceInfo.getDeviceType());
                
                // 获取配置文件路径
                String configPath = getConfigFilePath();
                log.info("使用配置文件路径: {}", configPath);
                
                // 验证配置文件是否存在
                File configFile = new File(configPath);
                if (!configFile.exists()) {
                    log.error("配置文件不存在: {}", configPath);
                    return false;
                }
                
                // 读取当前配置文件
                Map<String, Object> config = loadYamlConfig(configPath);
                if (config == null) {
                    log.error("无法加载配置文件: {}", configPath);
                    return false;
                }
                
                // 更新设备配置
                updateDeviceConfigInYaml(config, deviceInfo);
                
                // 保存配置文件
                saveYamlConfig(configPath, config);
                
                // 验证配置是否保存成功
                Map<String, Object> savedConfig = loadYamlConfig(configPath);
                if (savedConfig != null && isDeviceConfigUpdated(savedConfig, deviceInfo)) {
                    log.info("设备配置更新成功 - 设备ID: {}, 品牌: {}, 型号: {}", 
                        deviceInfo.getDeviceId(), deviceInfo.getDeviceBrand(), deviceInfo.getDeviceType());
                    return true;
                } else {
                    log.error("配置保存后验证失败");
                    return false;
                }
                
            } catch (Exception e) {
                log.error("设备配置更新失败 - 设备ID: {}", deviceInfo.getDeviceId(), e);
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
                
                // 方法1: 通过Spring Boot Actuator重启 (如果可用)
                try {
                    restartViaActuator();
                    log.info("通过Actuator重启项目成功");
                    return true;
                } catch (Exception e) {
                    log.warn("Actuator重启失败，尝试其他方式: {}", e.getMessage());
                }
                
                // 方法2: 通过外部脚本重启
                try {
                    restartViaScript();
                    log.info("通过脚本重启项目成功");
                    return true;
                } catch (Exception e) {
                    log.warn("脚本重启失败，尝试其他方式: {}", e.getMessage());
                }
                
                // 方法3: 通过JVM退出重启 (最后手段)
                try {
                    restartViaJvmExit();
                    log.info("通过JVM退出重启项目成功");
                    return true;
                } catch (Exception e) {
                    log.error("JVM退出重启失败: {}", e.getMessage());
                }
                
                log.warn("所有重启方式都失败，但配置已更新，需要手动重启");
                return false;
                
            } catch (Exception e) {
                log.error("项目重启失败", e);
                return false;
            }
        }, executorService);
    }
    
    /**
     * 通过Spring Boot Actuator重启
     */
    private void restartViaActuator() throws Exception {
        // 这里可以调用Spring Boot Actuator的restart端点
        // 需要添加spring-boot-starter-actuator依赖
        log.debug("尝试通过Actuator重启...");
        // 暂时跳过，因为可能没有配置Actuator
        throw new UnsupportedOperationException("Actuator restart not configured");
    }
    
    /**
     * 通过外部脚本重启
     */
    private void restartViaScript() throws Exception {
        log.debug("尝试通过脚本重启...");
        
        // 获取当前JAR文件路径
        String jarPath = getCurrentJarPath();
        if (jarPath == null) {
            throw new RuntimeException("无法获取当前JAR文件路径");
        }
        
        // 创建重启脚本
        String scriptContent = createRestartScript(jarPath);
        String scriptPath = saveRestartScript(scriptContent);
        
        // 执行重启脚本
        ProcessBuilder pb = new ProcessBuilder("bash", scriptPath);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectErrorStream(true); // 合并错误流和输出流
        
        Process process = pb.start();
        
        // 读取脚本输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.info("重启脚本输出: {}", line);
            }
        }
        
        // 等待脚本执行
        int exitCode = process.waitFor();
        log.info("重启脚本执行完成，退出码: {}", exitCode);
        
        if (exitCode != 0) {
            log.error("重启脚本执行失败，输出: {}", output.toString());
            throw new RuntimeException("重启脚本执行失败，退出码: " + exitCode + ", 输出: " + output.toString());
        }
        
        log.info("重启脚本执行成功，项目已重启");
    }
    
    /**
     * 通过JVM退出重启 (需要外部监控程序)
     */
    private void restartViaJvmExit() throws Exception {
        log.debug("尝试通过JVM退出重启...");
        
        // 创建一个延迟任务来退出JVM
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 等待2秒让响应返回
                log.info("正在退出JVM以触发重启...");
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        log.info("JVM退出任务已启动，将在2秒后退出");
    }
    
    /**
     * 获取当前JAR文件路径
     */
    private String getCurrentJarPath() {
        try {
            // 方法1: 通过系统属性获取
            String jarPath = System.getProperty("java.class.path");
            if (jarPath != null && jarPath.endsWith(".jar")) {
                return jarPath;
            }
            
            // 方法2: 通过当前类路径获取
            String classPath = this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().getPath();
            if (classPath.endsWith(".jar")) {
                return classPath;
            }
            
            // 方法3: 查找target目录下的JAR文件
            String userDir = System.getProperty("user.dir");
            File targetDir = new File(userDir, "target");
            if (targetDir.exists()) {
                File[] jarFiles = targetDir.listFiles((dir, name) -> 
                    name.endsWith(".jar") && !name.endsWith("-sources.jar"));
                if (jarFiles != null && jarFiles.length > 0) {
                    return jarFiles[0].getAbsolutePath();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取JAR文件路径失败", e);
            return null;
        }
    }
    
    /**
     * 创建重启脚本
     */
    private String createRestartScript(String jarPath) {
        return String.format("#!/bin/bash\n" +
            "set -e  # 遇到错误立即退出\n" +
            "echo \"[$(date)] 正在重启项目...\"\n" +
            "\n" +
            "# 停止现有的Java进程\n" +
            "echo \"[$(date)] 停止现有进程...\"\n" +
            "pkill -f \"unidbg-boot-server\" || echo \"没有找到现有进程\"\n" +
            "sleep 3\n" +
            "\n" +
            "# 确保端口9999被释放\n" +
            "echo \"[$(date)] 检查端口9999...\"\n" +
            "lsof -ti:9999 | xargs kill -9 2>/dev/null || echo \"端口9999已释放\"\n" +
            "sleep 2\n" +
            "\n" +
            "# 检查JAR文件是否存在\n" +
            "if [ ! -f \"%s\" ]; then\n" +
            "    echo \"[$(date)] 错误: JAR文件不存在: %s\"\n" +
            "    exit 1\n" +
            "fi\n" +
            "\n" +
            "# 启动新的JAR文件\n" +
            "echo \"[$(date)] 启动JAR文件: %s\"\n" +
            "cd \"%s\"\n" +
            "nohup java -jar \"%s\" > target/spring-boot.log 2>&1 &\n" +
            "JAVA_PID=$!\n" +
            "echo \"[$(date)] 新进程PID: $JAVA_PID\"\n" +
            "\n" +
            "# 等待进程启动\n" +
            "echo \"[$(date)] 等待进程启动...\"\n" +
            "sleep 5\n" +
            "\n" +
            "# 检查进程是否还在运行\n" +
            "if ps -p $JAVA_PID > /dev/null; then\n" +
            "    echo \"[$(date)] 项目重启成功，PID: $JAVA_PID\"\n" +
            "else\n" +
            "    echo \"[$(date)] 错误: 进程启动失败\"\n" +
            "    echo \"[$(date)] 检查日志: target/spring-boot.log\"\n" +
            "    tail -20 target/spring-boot.log\n" +
            "    exit 1\n" +
            "fi\n", 
            jarPath, jarPath, jarPath, System.getProperty("user.dir"), jarPath);
    }
    
    /**
     * 保存重启脚本
     */
    private String saveRestartScript(String scriptContent) throws IOException {
        String scriptPath = System.getProperty("user.dir") + "/restart.sh";
        try (FileWriter writer = new FileWriter(scriptPath)) {
            writer.write(scriptContent);
        }
        
        // 设置脚本执行权限
        File scriptFile = new File(scriptPath);
        scriptFile.setExecutable(true);
        
        return scriptPath;
    }

    /**
     * 注册设备并自动更新配置和重启项目
     */
    public CompletableFuture<DeviceManagementResult> registerDeviceAndRestart(DeviceRegisterRequest request) {
        // 如果未提供任何参数，则走与 tools/batch_device_register_xml.py 一致的随机注册流程
        boolean noExplicitParams = request == null || (
            request.getDeviceBrand() == null &&
            request.getDeviceType() == null &&
            request.getUseRealAlgorithm() == null &&
            request.getUseRealBrand() == null &&
            request.getAutoUpdateConfig() == null &&
            request.getAutoRestart() == null
        );

        if (noExplicitParams) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("未提供设备参数，使用脚本随机注册设备并同步配置");
                    // 1) 运行脚本生成设备
                    Path latestYaml = runPythonBatchRegisterAndGetLatestYaml();
                    if (latestYaml == null) {
                        return DeviceManagementResult.error("运行设备注册脚本失败或未生成配置");
                    }

                    // 2) 从 YAML 解析设备信息
                    DeviceInfo deviceInfo = parseDeviceInfoFromIndividualYaml(latestYaml);
                    if (deviceInfo == null) {
                        return DeviceManagementResult.error("解析设备配置失败: " + latestYaml);
                    }

                    // 3) 更新 application.yml
                    boolean configUpdated = updateDeviceConfig(deviceInfo).get();
                    if (!configUpdated) {
                        log.warn("脚本设备配置写入 application.yml 失败，请检查配置文件路径");
                    }

                    // 不在此处重启，交由外部脚本处理
                    return DeviceManagementResult.success("设备注册成功，已更新配置。请执行外部重启脚本。", deviceInfo);
                } catch (Exception e) {
                    log.error("脚本方式注册并重启失败", e);
                    return DeviceManagementResult.error("脚本方式注册失败: " + e.getMessage());
                }
            }, executorService);
        }

        return registerDevice(request)
            .thenCompose(registerResponse -> {
                if (!registerResponse.getSuccess()) {
                    return CompletableFuture.completedFuture(
                        DeviceManagementResult.error("设备注册失败: " + registerResponse.getMessage())
                    );
                }
                
                DeviceInfo deviceInfo = registerResponse.getDeviceInfo();
                log.info("设备注册成功，开始更新配置 - 设备ID: {}, 品牌: {}, 型号: {}", 
                    deviceInfo.getDeviceId(), deviceInfo.getDeviceBrand(), deviceInfo.getDeviceType());
                
                // 更新配置
                return updateDeviceConfig(deviceInfo)
                    .thenCompose(configSuccess -> {
                        if (!configSuccess) {
                            log.error("设备配置更新失败");
                            return CompletableFuture.completedFuture(
                                DeviceManagementResult.error("设备注册成功但更新配置失败")
                            );
                        } else {
                            log.info("设备配置更新成功");
                        }

                        // 不在此处重启，交由外部脚本处理
                        return CompletableFuture.completedFuture(
                            DeviceManagementResult.success("设备注册成功，已更新配置。请执行外部重启脚本。", deviceInfo)
                        );
                    });
            });
    }

    /**
     * 调用 tools/batch_device_register_xml.py 并返回最新 individual YAML 路径
     */
    private Path runPythonBatchRegisterAndGetLatestYaml() {
        try {
            String userDir = System.getProperty("user.dir");
            File projectRoot = new File(userDir);
            File script = new File(projectRoot, "tools/batch_device_register_xml.py");
            if (!script.exists()) {
                log.error("设备注册脚本不存在: {}", script.getAbsolutePath());
                return null;
            }

            ProcessBuilder pb = new ProcessBuilder("python3", script.getAbsolutePath());
            pb.directory(projectRoot);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("设备注册脚本输出: {}", line);
                }
            }

            int code = p.waitFor();
            if (code != 0) {
                log.error("设备注册脚本退出码非0: {}", code);
                return null;
            }

            // 定位 results/individual 下最新的 yaml
            Path individualDir = Paths.get(userDir, "results", "individual");
            if (!Files.exists(individualDir)) {
                log.error("设备配置目录不存在: {}", individualDir);
                return null;
            }
            try {
                return Files.list(individualDir)
                    .filter(p2 -> p2.getFileName().toString().endsWith(".yaml"))
                    .max(Comparator.comparingLong(p2 -> p2.toFile().lastModified()))
                    .orElse(null);
            } catch (IOException e) {
                log.error("遍历设备配置目录失败", e);
                return null;
            }
        } catch (Exception e) {
            log.error("运行设备注册脚本失败", e);
            return null;
        }
    }

    /**
     * 从 individual YAML 解析 DeviceInfo
     */
    @SuppressWarnings("unchecked")
    private DeviceInfo parseDeviceInfoFromIndividualYaml(Path yamlPath) {
        try (FileInputStream fis = new FileInputStream(yamlPath.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(fis);
            Map<String, Object> fq = (Map<String, Object>) root.get("fq");
            Map<String, Object> api = fq != null ? (Map<String, Object>) fq.get("api") : null;
            if (api == null) return null;
            Map<String, Object> device = (Map<String, Object>) api.get("device");
            if (device == null) return null;

            String cookie = (String) api.get("cookie");
            String userAgent = (String) api.get("user-agent");

            return DeviceInfo.builder()
                .aid((String) device.get("aid"))
                .cdid((String) device.get("cdid"))
                .deviceBrand((String) device.get("device-brand"))
                .deviceId((String) device.get("device-id"))
                .deviceType((String) device.get("device-type"))
                .dpi((String) device.get("dpi"))
                .hostAbi((String) device.get("host-abi"))
                .installId((String) device.get("install-id"))
                .resolution((String) device.get("resolution"))
                .romVersion((String) device.get("rom-version"))
                .updateVersionCode((String) device.get("update-version-code"))
                .versionCode((String) device.get("version-code"))
                .versionName((String) device.get("version-name"))
                .cookie(cookie)
                .userAgent(userAgent)
                .build();
        } catch (Exception e) {
            log.error("解析 individual YAML 失败: {}", yamlPath, e);
            return null;
        }
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
     * 验证设备配置是否已更新
     */
    @SuppressWarnings("unchecked")
    private boolean isDeviceConfigUpdated(Map<String, Object> config, DeviceInfo deviceInfo) {
        try {
            Map<String, Object> fqConfig = (Map<String, Object>) config.get("fq");
            if (fqConfig == null) return false;
            
            Map<String, Object> apiConfig = (Map<String, Object>) fqConfig.get("api");
            if (apiConfig == null) return false;
            
            Map<String, Object> deviceConfig = (Map<String, Object>) apiConfig.get("device");
            if (deviceConfig == null) return false;
            
            // 验证关键设备信息是否匹配
            String configDeviceId = (String) deviceConfig.get("device-id");
            String configDeviceBrand = (String) deviceConfig.get("device-brand");
            String configDeviceType = (String) deviceConfig.get("device-type");
            
            boolean deviceIdMatch = deviceInfo.getDeviceId() != null && 
                deviceInfo.getDeviceId().equals(configDeviceId);
            boolean deviceBrandMatch = deviceInfo.getDeviceBrand() != null && 
                deviceInfo.getDeviceBrand().equals(configDeviceBrand);
            boolean deviceTypeMatch = deviceInfo.getDeviceType() != null && 
                deviceInfo.getDeviceType().equals(configDeviceType);
            
            log.debug("配置验证 - 设备ID匹配: {}, 品牌匹配: {}, 型号匹配: {}", 
                deviceIdMatch, deviceBrandMatch, deviceTypeMatch);
            
            return deviceIdMatch && deviceBrandMatch && deviceTypeMatch;
        } catch (Exception e) {
            log.error("配置验证失败", e);
            return false;
        }
    }

    /**
     * 获取配置文件路径
     */
    private String getConfigFilePath() {
        if (configLocation.startsWith("classpath:")) {
            // 如果是classpath路径，尝试多种方式获取实际文件路径
            try {
                // 方法1: 通过ClassPathResource获取
                ClassPathResource resource = new ClassPathResource("application.yml");
                if (resource.exists()) {
                    return resource.getFile().getAbsolutePath();
                }
            } catch (Exception e) {
                log.warn("无法通过ClassPathResource获取配置文件路径: {}", e.getMessage());
            }
            
            try {
                // 方法2: 通过系统属性获取
                String userDir = System.getProperty("user.dir");
                String configPath = userDir + "/src/main/resources/application.yml";
                File configFile = new File(configPath);
                if (configFile.exists()) {
                    log.info("使用项目根目录下的配置文件: {}", configPath);
                    return configPath;
                }
            } catch (Exception e) {
                log.warn("无法通过项目根目录获取配置文件路径: {}", e.getMessage());
            }
            
            try {
                // 方法3: 通过当前工作目录获取
                String currentDir = System.getProperty("user.dir");
                String configPath = currentDir + "/src/main/resources/application.yml";
                File configFile = new File(configPath);
                if (configFile.exists()) {
                    log.info("使用当前目录下的配置文件: {}", configPath);
                    return configPath;
                }
            } catch (Exception e) {
                log.warn("无法通过当前目录获取配置文件路径: {}", e.getMessage());
            }
            
            log.error("无法获取配置文件路径，使用默认路径");
            return "src/main/resources/application.yml";
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
