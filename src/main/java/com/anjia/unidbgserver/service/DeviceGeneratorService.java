package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.dto.DeviceInfo;
import com.anjia.unidbgserver.dto.DeviceRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 设备生成服务
 * 基于Python版本的设备注册工具逻辑
 */
@Slf4j
@Service
public class DeviceGeneratorService {

    // 常见的Android设备品牌和型号
    private static final Map<String, List<String>> DEVICE_BRANDS = new HashMap<>();
    
    // Android版本信息
    private static final List<Map<String, Object>> ANDROID_VERSIONS = new ArrayList<>();
    
    // 分辨率选项
    private static final List<Map<String, Object>> RESOLUTIONS = new ArrayList<>();
    
    // CPU架构
    private static final List<String> CPU_ABIS = Arrays.asList("arm64-v8a", "armeabi-v7a");
    
    // ROM版本
    private static final List<String> ROM_VERSIONS = Arrays.asList(
        "1414", "1415", "1416", "1417", "1418", "1419", "1420"
    );

    static {
        // 初始化设备品牌
        DEVICE_BRANDS.put("Xiaomi", Arrays.asList(
            "24031PN0DC", "2304FPN6DC", "23078RKD5C", "23013RK75C", "22081212C",
            "2201123C", "21081111RG", "2107113SG", "2106118C", "2012123AC",
            "M2102K1AC", "M2011K2C", "M2007J1SC", "M2006C3LG", "RedmiK40",
            "RedmiK50", "MI11", "MI12", "MI13", "RedmiNote11", "RedmiNote12"
        ));
        
        DEVICE_BRANDS.put("HUAWEI", Arrays.asList(
            "ELS-AN00", "TAS-AL00", "ANA-AN00", "LYA-AL00", "VOG-AL00",
            "HMA-AL00", "JKM-AL00", "WLZ-AN00", "BAL-AL00", "CDL-AN00",
            "P50", "P40", "Mate40", "Mate50", "nova9", "nova10"
        ));
        
        DEVICE_BRANDS.put("OPPO", Arrays.asList(
            "CPH2207", "CPH2211", "CPH2237", "CPH2371", "CPH2399",
            "PDSM00", "PDST00", "PGBM10", "PGJM10", "PEQM00",
            "FindX5", "Reno8", "Reno9", "A96", "K10"
        ));
        
        DEVICE_BRANDS.put("vivo", Arrays.asList(
            "V2197A", "V2118A", "V2055A", "V2073A", "V2102A",
            "PD2186", "PD2194", "PD1986", "PD1955", "PD1924",
            "X80", "X90", "S15", "Y76s", "iQOO9"
        ));
        
        DEVICE_BRANDS.put("OnePlus", Arrays.asList(
            "LE2100", "LE2110", "MT2110", "MT2111", "PJZ110",
            "OnePlus9", "OnePlus10", "OnePlus11", "OnePlusNord"
        ));
        
        DEVICE_BRANDS.put("Samsung", Arrays.asList(
            "SM-G9980", "SM-G9910", "SM-G7810", "SM-G7730", "SM-A5260",
            "GalaxyS22", "GalaxyS23", "GalaxyNote20", "GalaxyA53"
        ));

        // 初始化Android版本
        ANDROID_VERSIONS.add(createAndroidVersion("12", 32, "V417IR"));
        ANDROID_VERSIONS.add(createAndroidVersion("13", 33, "V433IR"));
        ANDROID_VERSIONS.add(createAndroidVersion("11", 30, "V394IR"));
        ANDROID_VERSIONS.add(createAndroidVersion("10", 29, "V291IR"));
        ANDROID_VERSIONS.add(createAndroidVersion("14", 34, "V451IR"));

        // 初始化分辨率
        RESOLUTIONS.add(createResolution("1600*900", 320, "xhdpi"));
        RESOLUTIONS.add(createResolution("2400*1080", 480, "xxhdpi"));
        RESOLUTIONS.add(createResolution("2340*1080", 440, "xxhdpi"));
        RESOLUTIONS.add(createResolution("1920*1080", 480, "xxhdpi"));
        RESOLUTIONS.add(createResolution("2560*1440", 560, "xxxhdpi"));
        RESOLUTIONS.add(createResolution("3200*1440", 640, "xxxhdpi"));
    }

    private static Map<String, Object> createAndroidVersion(String version, Integer api, String release) {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("version", version);
        versionInfo.put("api", api);
        versionInfo.put("release", release);
        return versionInfo;
    }

    private static Map<String, Object> createResolution(String resolution, Integer densityDpi, String displayDensity) {
        Map<String, Object> resolutionInfo = new HashMap<>();
        resolutionInfo.put("resolution", resolution);
        resolutionInfo.put("density_dpi", densityDpi);
        resolutionInfo.put("display_density", displayDensity);
        return resolutionInfo;
    }

    /**
     * 生成设备信息
     */
    public DeviceInfo generateDeviceInfo(DeviceRegisterRequest request) {
        try {
            log.info("开始生成设备信息 - 品牌: {}, 型号: {}", request.getDeviceBrand(), request.getDeviceType());
            
            // 生成Android ID
            String androidId = generateAndroidId();
            
            // 生成OpenUDID
            String openudid = generateOpenUDID(androidId, request.getUseRealAlgorithm());
            
            // 生成设备型号
            String deviceBrand = request.getDeviceBrand();
            String deviceType = request.getDeviceType();
            
            if (deviceBrand == null || deviceType == null) {
                if (request.getUseRealBrand()) {
                    Map.Entry<String, List<String>> randomBrand = getRandomBrand();
                    deviceBrand = randomBrand.getKey();
                    deviceType = randomBrand.getValue().get(ThreadLocalRandom.current().nextInt(randomBrand.getValue().size()));
                } else {
                    deviceBrand = "Unknown";
                    deviceType = generateRandomDeviceModel();
                }
            }
            
            // 随机选择Android版本
            Map<String, Object> androidInfo = ANDROID_VERSIONS.get(ThreadLocalRandom.current().nextInt(ANDROID_VERSIONS.size()));
            
            // 随机选择分辨率
            Map<String, Object> screenInfo = RESOLUTIONS.get(ThreadLocalRandom.current().nextInt(RESOLUTIONS.size()));
            
            // 生成时间戳
            long currentTime = System.currentTimeMillis();
            long installTime = currentTime - ThreadLocalRandom.current().nextLong(86400000L, 31536000000L); // 1天到1年前
            
            // 生成设备标识符
            String deviceId = generateDeviceId();
            String installId = generateInstallId();
            String cdid = generateUUID();
            String sigHash = generateSigHash();
            String clientudid = generateUUID();
            String ipv6Address = generateIPv6();
            
            // 构建User-Agent
            String userAgent = String.format(
                "com.dragon.read.oversea.gp/68132 (Linux; U; Android %s; zh_CN; %s; Build/%s;tt-ok/3.12.13.4-tiktok)",
                androidInfo.get("version"),
                deviceType,
                androidInfo.get("release")
            );
            
            // 构建Cookie
            String cookie = String.format("store-region=cn-zj; store-region-src=did; install_id=%s;", installId);
            
            // 构建ROM版本
            String romVersion = String.format("%s+release-keys", androidInfo.get("release"));
            
            return DeviceInfo.builder()
                .deviceBrand(deviceBrand)
                .deviceType(deviceType)
                .deviceId(deviceId)
                .installId(installId)
                .cdid(cdid)
                .resolution((String) screenInfo.get("resolution"))
                .dpi(String.valueOf(screenInfo.get("density_dpi")))
                .hostAbi(CPU_ABIS.get(ThreadLocalRandom.current().nextInt(CPU_ABIS.size())))
                .romVersion(romVersion)
                .osVersion((String) androidInfo.get("version"))
                .osApi((Integer) androidInfo.get("api"))
                .userAgent(userAgent)
                .cookie(cookie)
                .aid("1967")
                .versionCode("68132")
                .versionName("6.8.1.32")
                .updateVersionCode("68132")
                .build();
                
        } catch (Exception e) {
            log.error("生成设备信息失败", e);
            return null;
        }
    }

    /**
     * 生成Android ID
     */
    private String generateAndroidId() {
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789abcdef";
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成OpenUDID
     */
    private String generateOpenUDID(String androidId, Boolean useRealAlgorithm) {
        if (useRealAlgorithm != null && useRealAlgorithm) {
            // 使用真实算法
            String char1 = md5Encode(androidId);
            String char2 = md5Encode(char1);
            return (char1 + char2.substring(0, 8)).toLowerCase();
        } else {
            // 使用随机算法
            StringBuilder sb = new StringBuilder();
            String chars = "0123456789abcdef";
            for (int i = 0; i < 40; i++) {
                sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
            }
            return sb.toString();
        }
    }

    /**
     * MD5编码
     */
    private String md5Encode(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5编码失败", e);
            return text;
        }
    }

    /**
     * 生成设备ID
     */
    private String generateDeviceId() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1000000000000000L, 9999999999999999L));
    }

    /**
     * 生成安装ID
     */
    private String generateInstallId() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1000000000000000L, 9999999999999999L));
    }

    /**
     * 生成UUID
     */
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成签名哈希
     */
    private String generateSigHash() {
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789abcdef";
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成IPv6地址
     */
    private String generateIPv6() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%04X", ThreadLocalRandom.current().nextInt(0, 65536)));
        }
        return sb.toString();
    }

    /**
     * 获取随机品牌
     */
    private Map.Entry<String, List<String>> getRandomBrand() {
        List<Map.Entry<String, List<String>>> entries = new ArrayList<>(DEVICE_BRANDS.entrySet());
        return entries.get(ThreadLocalRandom.current().nextInt(entries.size()));
    }

    /**
     * 生成随机设备型号
     */
    private String generateRandomDeviceModel() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder model = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 3) {
                model.append('-');
            } else {
                model.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
            }
        }
        return model.toString();
    }
}
