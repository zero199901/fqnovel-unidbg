#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import random
import time
import uuid
import hashlib
import requests
import yaml
from datetime import datetime
from typing import Dict, Any, List
import os
import secrets

class ImprovedRandomDeviceGenerator:
    """改进的随机设备信息生成器，基于真实的openudid算法"""
    
    # 常见的Android设备品牌和型号
    DEVICE_BRANDS = {
        "Xiaomi": [
            "24031PN0DC", "2304FPN6DC", "23078RKD5C", "23013RK75C", "22081212C",
            "2201123C", "21081111RG", "2107113SG", "2106118C", "2012123AC",
            "M2102K1AC", "M2011K2C", "M2007J1SC", "M2006C3LG", "RedmiK40",
            "RedmiK50", "MI11", "MI12", "MI13", "RedmiNote11", "RedmiNote12"
        ],
        "HUAWEI": [
            "ELS-AN00", "TAS-AL00", "ANA-AN00", "LYA-AL00", "VOG-AL00",
            "HMA-AL00", "JKM-AL00", "WLZ-AN00", "BAL-AL00", "CDL-AN00",
            "P50", "P40", "Mate40", "Mate50", "nova9", "nova10"
        ],
        "OPPO": [
            "CPH2207", "CPH2211", "CPH2237", "CPH2371", "CPH2399",
            "PDSM00", "PDST00", "PGBM10", "PGJM10", "PEQM00",
            "FindX5", "Reno8", "Reno9", "A96", "K10"
        ],
        "vivo": [
            "V2197A", "V2118A", "V2055A", "V2073A", "V2102A",
            "PD2186", "PD2194", "PD1986", "PD1955", "PD1924",
            "X80", "X90", "S15", "Y76s", "iQOO9"
        ],
        "OnePlus": [
            "LE2100", "LE2110", "MT2110", "MT2111", "PJZ110",
            "OnePlus9", "OnePlus10", "OnePlus11", "OnePlusNord"
        ],
        "Samsung": [
            "SM-G9980", "SM-G9910", "SM-G7810", "SM-G7730", "SM-A5260",
            "GalaxyS22", "GalaxyS23", "GalaxyNote20", "GalaxyA53"
        ]
    }
    
    # Android版本信息
    ANDROID_VERSIONS = [
        {"version": "12", "api": 32, "release": "V417IR"},
        {"version": "13", "api": 33, "release": "V433IR"},
        {"version": "11", "api": 30, "release": "V394IR"},
        {"version": "10", "api": 29, "release": "V291IR"},
        {"version": "14", "api": 34, "release": "V451IR"}
    ]
    
    # 分辨率选项 (注意格式用*而不是x，符合XML配置)
    RESOLUTIONS = [
        {"resolution": "1600*900", "density_dpi": 320, "display_density": "xhdpi"},
        {"resolution": "2400*1080", "density_dpi": 480, "display_density": "xxhdpi"},
        {"resolution": "2340*1080", "density_dpi": 440, "display_density": "xxhdpi"},
        {"resolution": "1920*1080", "density_dpi": 480, "display_density": "xxhdpi"},
        {"resolution": "2560*1440", "density_dpi": 560, "display_density": "xxxhdpi"},
        {"resolution": "3200*1440", "density_dpi": 640, "display_density": "xxxhdpi"}
    ]
    
    # CPU架构
    CPU_ABIS = ["arm64-v8a", "armeabi-v7a"]
    
    # ROM版本
    ROM_VERSIONS = [
        "1414", "1415", "1416", "1417", "1418", "1419", "1420"
    ]
    
    @staticmethod
    def md5_encode(text: str) -> str:
        """MD5编码函数，模拟Java的md5Encode"""
        return hashlib.md5(text.encode('utf-8')).hexdigest()
    
    @staticmethod
    def generate_android_id() -> str:
        """生成模拟的Android ID (16位hex字符串)"""
        return ''.join(random.choices('0123456789abcdef', k=16))
    
    @staticmethod
    def generate_openudid_real(android_id: str = None) -> str:
        """
        基于真实算法生成OpenUDID
        算法: char = md5(androidId()); udid = char + md5(char).slice(0, 8)
        """
        if android_id is None:
            android_id = ImprovedRandomDeviceGenerator.generate_android_id()
        
        # 第一步：对android_id进行MD5
        char = ImprovedRandomDeviceGenerator.md5_encode(android_id)
        
        # 第二步：对第一步结果再进行MD5，取前8位
        char_md5 = ImprovedRandomDeviceGenerator.md5_encode(char)
        
        # 第三步：拼接成40位的openudid
        udid = char + char_md5[:8]
        
        return udid.lower()
    
    @staticmethod
    def generate_device_id() -> str:
        """生成设备ID（数字格式）"""
        return str(random.randint(1000000000000000, 9999999999999999))
    
    @staticmethod
    def generate_install_id() -> str:
        """生成安装ID（数字格式）"""
        return str(random.randint(1000000000000000, 9999999999999999))
    
    @staticmethod
    def generate_uuid() -> str:
        """生成UUID"""
        return str(uuid.uuid4())
    
    @staticmethod
    def generate_sig_hash() -> str:
        """生成签名哈希"""
        return ''.join(random.choices('0123456789abcdef', k=32))
    
    @staticmethod
    def generate_ipv6() -> str:
        """生成IPv6地址"""
        segments = []
        for _ in range(8):
            segment = f"{random.randint(0, 65535):04X}"
            segments.append(segment)
        return ":".join(segments)
    
    @classmethod
    def generate_random_device(cls, use_real_algorithm: bool = True, 
                              use_real_brand_model: bool = True) -> Dict[str, Any]:
        """
        生成随机设备信息，格式符合XML配置要求
        
        Args:
            use_real_algorithm: 是否使用真实的openudid算法
            use_real_brand_model: 是否使用真实的品牌型号
        """
        # 生成Android ID
        android_id = cls.generate_android_id()
        
        # 生成OpenUDID
        if use_real_algorithm:
            openudid = cls.generate_openudid_real(android_id)
        else:
            openudid = ''.join(random.choices('0123456789abcdef', k=40))
        
        # 生成设备型号
        if use_real_brand_model:
            # 使用真实品牌和型号（无空格格式）
            brand = random.choice(list(cls.DEVICE_BRANDS.keys()))
            model = random.choice(cls.DEVICE_BRANDS[brand])
        else:
            # 使用随机算法生成型号
            brand = "Unknown"
            model = cls.generate_device_model_random()
        
        # 随机选择Android版本
        android_info = random.choice(cls.ANDROID_VERSIONS)
        
        # 随机选择分辨率
        screen_info = random.choice(cls.RESOLUTIONS)
        
        # 生成时间戳
        current_time = int(time.time() * 1000)
        install_time = current_time - random.randint(86400000, 31536000000)  # 1天到1年前
        
        # 生成设备标识符
        device_id = cls.generate_device_id()
        install_id = cls.generate_install_id()
        
        device_info = {
            "android_id": android_id,
            "device_brand": brand,
            "device_manufacturer": brand,
            "device_model": model,
            "device_type": model,  # XML格式中的device-type，无空格
            "os_version": android_info["version"],
            "os_api": android_info["api"],
            "release_build": android_info["release"] + "_20171120",
            "rom_version": f"{android_info['release']}+release-keys",  # XML格式，用+连接
            "resolution": screen_info["resolution"],  # 使用*格式
            "density_dpi": screen_info["density_dpi"],
            "display_density": screen_info["display_density"],
            "cpu_abi": random.choice(cls.CPU_ABIS),
            "host_abi": random.choice(cls.CPU_ABIS),  # XML格式中的host-abi
            "rom": random.choice(cls.ROM_VERSIONS),
            "cdid": cls.generate_uuid(),
            "sig_hash": cls.generate_sig_hash(),
            "openudid": openudid,
            "clientudid": cls.generate_uuid(),
            "ipv6_address": cls.generate_ipv6(),
            "device_id": device_id,
            "install_id": install_id,
            "req_id": cls.generate_uuid(),
            "apk_first_install_time": install_time,
            "_gen_time": current_time,
            "_rticket": current_time,
            "algorithm_info": {
                "use_real_openudid_algorithm": use_real_algorithm,
                "use_real_brand_model": use_real_brand_model,
                "openudid_generation": "real" if use_real_algorithm else "random"
            }
        }
        
        return device_info
    
    @staticmethod
    def generate_device_model_random() -> str:
        """
        生成随机设备型号（无空格格式）
        """
        char = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
        model = ""
        for i in range(8):
            if i == 3:
                model += '-'
            else:
                model += char[random.randint(0, len(char) - 1)]
        return model

class EnhancedDeviceRegisterClient:
    """增强的设备注册客户端"""
    
    def __init__(self):
        self.base_url = "https://log5-applog.fqnovel.com/service/2/device_register/"
        self.app_info = {
            "display_name": "番茄小说",
            "aid": 1967,
            "channel": "googleplay",
            "package": "com.dragon.read.oversea.gp",
            "app_version": "6.8.1.32",
            "version_code": 68132,
            "update_version_code": 68132,
            "manifest_version_code": 68132,
            "app_version_minor": "6.8.1.32",
            "sdk_version": "3.7.0-rc.25-fanqie-xiaoshuo-opt",
            "sdk_target_version": 29,
            "git_hash": "5b6a0d3",
            "sdk_flavor": "china",
            "guest_mode": 0,
            "is_system_app": 0,
            "pre_installed_channel": "",
            "not_request_sender": 0
        }
    
    def build_headers(self, device_info: Dict[str, Any]) -> Dict[str, str]:
        """构建请求头"""
        return {
            "User-Agent": f"com.dragon.read.oversea.gp/68132 (Linux; U; Android {device_info['os_version']}; zh_CN; {device_info['device_model']}; Build/{device_info['rom_version'].split('+')[0]};tt-ok/3.12.13.4-tiktok)",
            "Accept": "application/json",
            "Accept-Encoding": "gzip",
            "Content-Type": "application/json",
            "log-encode-type": "gzip",
            "x-ss-req-ticket": str(device_info["_gen_time"]),
            "x-vc-bdturing-sdk-version": "3.7.2.cn",
            "Cookie": f"store-region=cn-zj; store-region-src=did; install_id={device_info['install_id']}"
        }
    
    def build_params(self, device_info: Dict[str, Any]) -> Dict[str, str]:
        """构建URL参数"""
        return {
            "aid": str(self.app_info["aid"]),
            "version_code": str(self.app_info["version_code"]),
            "channel": self.app_info["channel"],
            "package": self.app_info["package"],
            "_rticket": str(device_info["_rticket"]),
            "use_store_region_cookie": "1",
            "okhttp_version": "4.2.137.76-fanqie"
        }
    
    def build_payload(self, device_info: Dict[str, Any]) -> Dict[str, Any]:
        """构建请求负载"""
        # 合并应用信息和设备信息
        header = {**self.app_info}
        header.update({
            "os": "Android",
            "os_version": device_info["os_version"],
            "os_api": device_info["os_api"],
            "device_model": device_info["device_model"],
            "device_brand": device_info["device_brand"],
            "device_manufacturer": device_info["device_manufacturer"],
            "cpu_abi": device_info["cpu_abi"],
            "release_build": device_info["release_build"],
            "density_dpi": device_info["density_dpi"],
            "display_density": device_info["display_density"],
            "resolution": device_info["resolution"].replace("*", "x"),  # 在payload中使用x格式
            "language": "zh",
            "timezone": 8,
            "access": "wifi",
            "rom": device_info["rom"],
            "rom_version": device_info["rom_version"].replace("+", " "),  # 在payload中使用空格格式
            "cdid": device_info["cdid"],
            "sig_hash": device_info["sig_hash"],
            "openudid": device_info["openudid"],
            "clientudid": device_info["clientudid"],
            "ipv6_list": [
                {
                    "type": "client_anpi",
                    "value": device_info["ipv6_address"]
                }
            ],
            "region": "CN",
            "tz_name": "Asia/Shanghai",
            "tz_offset": 28800,
            "sim_serial_number": [],
            "oaid_may_support": False,
            "req_id": device_info["req_id"],
            "device_platform": "android",
            "custom": {
                "host_bit": 64,
                "account_region": "cn",
                "dragon_device_type": "phone"
            },
            "apk_first_install_time": device_info["apk_first_install_time"]
        })
        
        return {
            "magic_tag": "ss_app_log",
            "header": header,
            "_gen_time": device_info["_gen_time"]
        }
    
    def register_device(self, device_info: Dict[str, Any]) -> Dict[str, Any]:
        """注册设备"""
        headers = self.build_headers(device_info)
        params = self.build_params(device_info)
        payload = self.build_payload(device_info)
        
        algorithm_type = "真实算法" if device_info.get("algorithm_info", {}).get("use_real_openudid_algorithm") else "随机算法"
        print(f"[INFO] 开始注册设备: {device_info['device_brand']} {device_info['device_model']}")
        print(f"[INFO] OpenUDID算法: {algorithm_type}")
        print(f"[INFO] 设备ID: {device_info['device_id']}")
        print(f"[INFO] 安装ID: {device_info['install_id']}")
        
        try:
            response = requests.post(
                self.base_url,
                params=params,
                headers=headers,
                json=payload,
                timeout=30,
                verify=True
            )
            
            result = {
                "timestamp": datetime.utcnow().isoformat() + "Z",
                "request": {
                    "url": response.url,
                    "headers": dict(headers),
                    "params": params,
                    "payload": payload,
                    "device_info": device_info
                },
                "response": {
                    "status_code": response.status_code,
                    "headers": dict(response.headers),
                    "content": response.text,
                    "success": response.status_code == 200
                }
            }
            
            # 尝试解析JSON响应
            try:
                response_json = response.json()
                result["response"]["json"] = response_json
                print(f"[SUCCESS] 注册成功! 状态码: {response.status_code}")
                
                # 更新设备信息（如果服务器返回了新的ID）
                if "device_id" in response_json:
                    device_info["device_id"] = str(response_json["device_id"])
                    print(f"[INFO] 服务器返回设备ID: {response_json['device_id']}")
                if "install_id" in response_json:
                    device_info["install_id"] = str(response_json["install_id"])
                    print(f"[INFO] 服务器返回安装ID: {response_json['install_id']}")
                    
            except json.JSONDecodeError:
                print(f"[WARNING] 响应不是有效的JSON: {response.text[:100]}...")
            
            return result
            
        except requests.exceptions.RequestException as e:
            error_result = {
                "timestamp": datetime.utcnow().isoformat() + "Z",
                "request": {
                    "device_info": device_info,
                    "headers": dict(headers),
                    "params": params,
                    "payload": payload
                },
                "error": str(e),
                "success": False
            }
            print(f"[ERROR] 注册失败: {e}")
            return error_result

def generate_xml_config(device_info: Dict[str, Any]) -> Dict[str, Any]:
    """生成XML格式的配置"""
    return {
        "fq": {
            "api": {
                "base-url": "https://api5-normal-sinfonlineb.fqnovel.com",
                "user-agent": f"com.dragon.read.oversea.gp/68132 (Linux; U; Android {device_info['os_version']}; zh_CN; {device_info['device_type']}; Build/{device_info['rom_version'].split('+')[0]};tt-ok/3.12.13.4-tiktok)",
                "cookie": f"store-region=cn-zj; store-region-src=did; install_id={device_info['install_id']};",
                "device": {
                    "cdid": device_info["cdid"],
                    "install-id": device_info["install_id"],
                    "device-id": device_info["device_id"],
                    "aid": "1967",
                    "version-code": "68132",
                    "version-name": "6.8.1.32",
                    "update-version-code": "68132",
                    "device-type": device_info["device_type"],
                    "device-brand": device_info["device_brand"],
                    "rom-version": device_info["rom_version"],
                    "resolution": device_info["resolution"],
                    "dpi": str(device_info["density_dpi"]),
                    "host-abi": device_info["host_abi"]
                }
            }
        }
    }

def save_results(results: List[Dict[str, Any]], xml_configs: List[Dict[str, Any]], 
                timestamp: str) -> tuple[str, str]:
    """保存结果到文件"""
    # 确保results目录存在
    os.makedirs("results", exist_ok=True)
    
    # 保存全量信息
    full_results_file = os.path.join("results", f"device_register_full_{timestamp}.json")
    with open(full_results_file, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    # 保存XML配置格式信息
    xml_configs_file = os.path.join("results", f"device_register_xml_configs_{timestamp}.yaml")
    with open(xml_configs_file, 'w', encoding='utf-8') as f:
        yaml.dump({"devices": xml_configs}, f, default_flow_style=False, allow_unicode=True)
    
    # 额外保存单独的XML配置文件
    for i, config in enumerate(xml_configs):
        single_xml_file = os.path.join("results", f"device_config_{i+1}_{timestamp}.yaml")
        with open(single_xml_file, 'w', encoding='utf-8') as f:
            yaml.dump(config, f, default_flow_style=False, allow_unicode=True)
    
    print(f"[INFO] 全量结果已保存到: {full_results_file}")
    print(f"[INFO] XML配置已保存到: {xml_configs_file}")
    print(f"[INFO] 已生成 {len(xml_configs)} 个单独的配置文件")
    
    return full_results_file, xml_configs_file

def batch_register_devices(count: int, use_real_algorithm: bool = True, 
                          use_real_brand: bool = True) -> tuple[List[Dict], List[Dict]]:
    """批量注册设备"""
    client = EnhancedDeviceRegisterClient()
    results = []
    xml_configs = []
    
    print(f"\n开始批量注册 {count} 个设备...")
    print(f"OpenUDID算法: {'真实算法' if use_real_algorithm else '随机算法'}")
    print(f"设备型号: {'真实品牌' if use_real_brand else '随机生成'}")
    print("延迟设置: 每次注册间隔 3-5 秒")
    print("=" * 60)
    
    for i in range(count):
        print(f"\n--- 注册设备 {i+1}/{count} ---")
        
        # 生成设备信息
        device_info = ImprovedRandomDeviceGenerator.generate_random_device(
            use_real_algorithm=use_real_algorithm,
            use_real_brand_model=use_real_brand
        )
        
        # 注册设备
        result = client.register_device(device_info)
        results.append(result)
        
        # 生成XML配置
        if result.get("response", {}).get("success"):
            xml_config = generate_xml_config(device_info)
            xml_configs.append(xml_config)
            
            print(f"✓ 设备 {i+1} 注册成功")
            print(f"  设备类型: {device_info['device_brand']} {device_info['device_type']}")
            print(f"  设备ID: {device_info['device_id']}")
            print(f"  安装ID: {device_info['install_id']}")
            print(f"  分辨率: {device_info['resolution']}")
            print(f"  ROM版本: {device_info['rom_version']}")
        else:
            print(f"✗ 设备 {i+1} 注册失败")
        
        # 添加延迟避免请求过快 (3-5秒)
        if i < count - 1:
            delay = random.uniform(3, 5)
            print(f"[INFO] 等待 {delay:.1f} 秒后继续...")
            time.sleep(delay)
    
    return results, xml_configs

def main():
    """主函数"""
    print("批量设备注册工具 (XML配置格式)")
    print("=" * 60)
    
    # 询问用户配置
    try:
        count = int(input("请输入要注册的设备数量 (默认5): ") or "5")
    except ValueError:
        count = 5
    
    if count <= 0:
        count = 5
    
    # 固定使用真实算法和真实品牌
    use_real_algorithm = True
    use_real_brand = True
    
    print(f"\n配置确认:")
    print(f"- 注册数量: {count}")
    print(f"- OpenUDID算法: 真实算法")
    print(f"- 设备型号: 真实品牌")
    print(f"- 延迟设置: 3-5秒随机间隔")
    
    # 批量注册设备
    results, xml_configs = batch_register_devices(count, use_real_algorithm, use_real_brand)
    
    # 保存结果
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    full_file, xml_file = save_results(results, xml_configs, timestamp)
    
    # 统计结果
    successful = sum(1 for r in results if r.get("response", {}).get("success"))
    failed = count - successful
    
    print(f"\n{'='*60}")
    print(f"批量注册完成统计:")
    print(f"成功: {successful}")
    print(f"失败: {failed}")
    print(f"总计: {count}")
    print(f"成功率: {successful/count*100:.1f}%")
    print(f"XML配置文件数量: {len(xml_configs)}")
    
    # 显示第一个XML配置示例
    if xml_configs:
        print(f"\n--- XML配置示例 ---")
        print(yaml.dump(xml_configs[0], default_flow_style=False, allow_unicode=True))
        
        # 显示设备信息摘要
        print(f"\n--- 成功注册的设备摘要 ---")
        for i, config in enumerate(xml_configs[:5]):  # 显示前5个
            device = config["fq"]["api"]["device"]
            print(f"{i+1}. {device['device-brand']} {device['device-type']}")
            print(f"   设备ID: {device['device-id']}")
            print(f"   安装ID: {device['install-id']}")
            print(f"   分辨率: {device['resolution']}")
            print()
        
        if len(xml_configs) > 5:
            print(f"... 还有 {len(xml_configs)-5} 个设备")

if __name__ == "__main__":
    main()