package com.anjia.unidbgserver.dto;

import com.anjia.unidbgserver.config.FQApiProperties;
import lombok.Data;

/**
 * FQNovel API 变量配置
 * 对应 Rust 中的 FqVariable 结构
 */
@Data
public class FqVariable {

    /**
     * 安装ID
     */
    private String installId;

    /**
     * 服务端设备ID
     */
    private String serverDeviceId;

    /**
     * 应用ID
     */
    private String aid;

    /**
     * 更新版本代码
     */
    private String updateVersionCode;

    // 新增完整的API参数
    private String keyRegisterTs;
    private String deviceId;
    private String ac;
    private String channel;
    private String appName;
    private String versionCode;
    private String versionName;
    private String devicePlatform;
    private String os;
    private String ssmix;
    private String deviceType;
    private String deviceBrand;
    private String language;
    private String osApi;
    private String osVersion;
    private String manifestVersionCode;
    private String resolution;
    private String dpi;
    private String rticket;
    private String hostAbi;
    private String dragonDeviceType;
    private String pvPlayer;
    private String complianceStatus;
    private String needPersonalRecommend;
    private String playerSoLoad;
    private String isAndroidPadScreen;
    private String romVersion;
    private String cdid;

    /**
     * 从配置构造
     */
    public FqVariable(FQApiProperties fqApiProperties) {
        if (fqApiProperties != null && fqApiProperties.getDevice() != null) {
            FQApiProperties.Device device = fqApiProperties.getDevice();
            this.installId = device.getInstallId();
            this.serverDeviceId = device.getDeviceId();
            this.aid = device.getAid();
            this.updateVersionCode = device.getUpdateVersionCode();
            initializeFromConfig(device);
        } else {
            initializeDefaults();
        }
    }

    /**
     * 构造函数
     */
    public FqVariable(String installId, String serverDeviceId, String aid, String updateVersionCode) {
        this.installId = installId;
        this.serverDeviceId = serverDeviceId;
        this.aid = aid;
        this.updateVersionCode = updateVersionCode;
        initializeDefaults();
    }

    /**
     * 默认构造函数
     */
    public FqVariable() {
        initializeDefaults();
    }

    /**
     * 从配置初始化
     */
    private void initializeFromConfig(FQApiProperties.Device device) {
        this.keyRegisterTs = "0";
        this.deviceId = device.getDeviceId();
        this.ac = "wifi";
        this.channel = "googleplay";
        this.appName = "novelapp";
        this.versionCode = device.getVersionCode();
        this.versionName = device.getVersionName();
        this.devicePlatform = "android";
        this.os = "android";
        this.ssmix = "a";
        this.deviceType = device.getDeviceType();
        this.deviceBrand = device.getDeviceBrand();
        this.language = "zh";
        this.osApi = "32";
        this.osVersion = "13";
        this.manifestVersionCode = device.getVersionCode();
        this.resolution = device.getResolution();
        this.dpi = device.getDpi();
        this.rticket = String.valueOf(System.currentTimeMillis());
        this.hostAbi = device.getHostAbi();
        this.dragonDeviceType = "phone";
        this.pvPlayer = device.getVersionCode();
        this.complianceStatus = "0";
        this.needPersonalRecommend = "1";
        this.playerSoLoad = "1";
        this.isAndroidPadScreen = "0";
        this.romVersion = device.getRomVersion();
        this.cdid = device.getCdid();
    }

    /**
     * 初始化默认值（向后兼容）
     */
    private void initializeDefaults() {
        this.installId = "933935730456617";
        this.serverDeviceId = "933935730452521";
        this.aid = "1967";
        this.updateVersionCode = "68132";

        this.keyRegisterTs = "0";
        this.deviceId = this.serverDeviceId; // 使用serverDeviceId作为deviceId
        this.ac = "wifi";
        this.channel = "googleplay";
        this.appName = "novelapp";
        this.versionCode = "68132";
        this.versionName = "6.8.1.32";
        this.devicePlatform = "android";
        this.os = "android";
        this.ssmix = "a";
        this.deviceType = "24031PN0DC";
        this.deviceBrand = "Xiaomi";
        this.language = "zh";
        this.osApi = "32";
        this.osVersion = "12";
        this.manifestVersionCode = "68132";
        this.resolution = "3200*1440";
        this.dpi = "320";
        this.rticket = String.valueOf(System.currentTimeMillis());
        this.hostAbi = "arm64-v8a";
        this.dragonDeviceType = "phone";
        this.pvPlayer = "68132";
        this.complianceStatus = "0";
        this.needPersonalRecommend = "1";
        this.playerSoLoad = "1";
        this.isAndroidPadScreen = "0";
        this.romVersion = "V291IR+release-keys";
        this.cdid = "17f05006-423a-4172-be4b-7d26a42f2f4a";
    }
}
