# FQNovel Unidbg 项目状态文档

## 📊 项目概览

**项目名称**: fqnovel-unidbg  
**当前分支**: 1013new  
**最新版本**: 9ed3037  
**状态**: ✅ 稳定运行  

## 🏗️ 项目架构

### 核心服务
- **FQNovelService**: 小说章节内容获取服务
- **DeviceManagementService**: 设备注册和管理服务
- **FQEncryptService**: FQ签名加密服务
- **FullBookDownloadService**: 全本下载服务

### API接口
- **端口**: 9999
- **基础URL**: http://127.0.0.1:9999

## 🚀 主要功能

### 1. 设备注册功能 ✅

**API端点**:
- `POST /api/device/register` - 设备注册
- `POST /api/device/register-and-restart` - 设备注册并重启
- `GET /api/device/health` - 设备管理服务健康检查

**功能特点**:
- 自动生成设备信息（品牌、型号、ID等）
- 支持自动重启服务
- 动态更新配置文件
- 错误处理和日志记录

**测试结果**:
```json
{
  "success": true,
  "message": "设备注册成功",
  "deviceInfo": {
    "deviceBrand": "vivo",
    "deviceType": "PD2186",
    "deviceId": "8897460456777783",
    "installId": "9572027561010387",
    "cdid": "d28366e2-2b1e-4226-996d-6107f823a81a",
    "resolution": "2400*1080",
    "dpi": "480",
    "hostAbi": "armeabi-v7a",
    "romVersion": "V433IR+release-keys",
    "osVersion": "13",
    "osApi": 33
  }
}
```

### 2. 章节批量获取功能 ✅

**API端点**:
- `POST /api/fqnovel/chapters/batch` - 批量获取章节内容
- `GET /api/fqnovel/chapter/{bookId}/{chapterId}` - 获取单个章节
- `GET /api/fqnovel/book/{bookId}` - 获取书籍信息

**功能特点**:
- 支持批量章节内容获取
- 自动解密和解压缩
- 返回HTML和纯文本内容
- 字数统计和免费状态判断

**测试结果**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "bookId": "6707112755507235848",
    "bookInfo": {
      "bookName": "我居然是超级富二代",
      "author": "乡野水人",
      "totalChapters": 1836998
    },
    "chapters": {
      "6707197312789119502": {
        "chapterName": "第01章，银行打进一笔巨款",
        "rawContent": "<html>原始HTML内容</html>",
        "txtContent": "纯文本内容",
        "wordCount": 1535,
        "isFree": true
      }
    }
  }
}
```

### 3. 全本下载功能 ✅

**API端点**:
- `GET /api/fullbook/download` - 全本下载
- `POST /api/fullbook/download` - 全本下载（POST方式）

**功能特点**:
- 支持整本书下载
- 自动章节范围处理
- 文件生成和管理
- 进度跟踪

## 🔧 技术实现

### 核心技术栈
- **Java 11+**
- **Spring Boot 2.x**
- **Unidbg** - Android模拟环境
- **Redis** - 缓存和配置存储
- **Jackson** - JSON处理
- **AES-128-CBC** - 内容加密

### 关键配置

**application.yml**:
```yaml
server:
  port: 9999
  address: 0.0.0.0

fq:
  api:
    base-url: https://api5-normal-sinfonlineb.fqnovel.com
    user-agent: com.dragon.read.oversea.gp/68132 (Linux; U; Android 13; zh_CN; PD2186; Build/V433IR;tt-ok/3.12.13.4-tiktok)
    cookie: store-region=cn-zj; store-region-src=did; install_id=9572027561010387;
    device:
      aid: '1967'
      cdid: d28366e2-2b1e-4226-996d-6107f823a81a
      device-brand: vivo
      device-id: '8897460456777783'
      device-type: PD2186
      dpi: '480'
      host-abi: armeabi-v7a
      install-id: '9572027561010387'
      resolution: 2400*1080
      rom-version: V433IR+release-keys
      update-version-code: '68132'
      version-code: '68132'
      version-name: 6.8.1.32

spring:
  application:
    name: unidbg-boot-server
  profiles:
    active: dev
  redis:
    host: 60.205.188.67
    port: 26586
    password: HT5aRYBK2HpReaQuYkLcg
    database: 0
```

## 📁 项目结构

```
fqnovel-unidbg/
├── src/main/java/com/anjia/unidbgserver/
│   ├── config/                 # 配置类
│   │   ├── FQApiProperties.java
│   │   ├── JacksonConfig.java
│   │   └── UnidbgProperties.java
│   ├── dto/                    # 数据传输对象
│   │   ├── FQNovelRequest.java
│   │   ├── FQNovelResponse.java
│   │   └── ...
│   ├── service/                # 业务服务
│   │   ├── FQNovelService.java
│   │   ├── DeviceManagementService.java
│   │   ├── FQEncryptService.java
│   │   └── FullBookDownloadService.java
│   ├── web/                    # Web控制器
│   │   ├── FQNovelController.java
│   │   ├── DeviceManagementController.java
│   │   └── FullBookDownloadController.java
│   └── utils/                  # 工具类
│       ├── FQApiUtils.java
│       └── TempFileUtils.java
├── src/main/resources/
│   ├── application.yml         # 主配置文件
│   ├── application-dev.yml     # 开发环境配置
│   └── com/dragon/read/oversea/gp/  # Unidbg资源文件
├── tools/                      # 工具脚本
│   ├── batch_device_register_xml.py
│   └── export_book_cached_merge.py
└── results/                    # 结果输出目录
    ├── configs/               # 设备配置
    ├── individual/            # 单个设备信息
    ├── novels/               # 小说内容
    └── reports/              # 报告文件
```

## 🧪 测试验证

### 健康检查
```bash
# FQNovel服务
curl -X GET 'http://127.0.0.1:9999/api/fqnovel/health'

# 设备管理服务
curl -X GET 'http://127.0.0.1:9999/api/device/health'
```

### 功能测试
```bash
# 设备注册
curl -X POST 'http://127.0.0.1:9999/api/device/register' \
  -H 'Content-Type: application/json' -d '{}'

# 章节批量获取
curl -X POST 'http://127.0.0.1:9999/api/fqnovel/chapters/batch' \
  -H 'Content-Type: application/json' \
  -d '{
    "bookId": "6707112755507235848",
    "chapterIds": ["6707197312789119502"]
  }'

# 书籍信息获取
curl -X GET 'http://127.0.0.1:9999/api/fqnovel/book/6707112755507235848'
```

## 📈 版本历史

### 当前版本 (9ed3037)
- ✅ 设备注册功能完善
- ✅ 章节批量获取功能稳定
- ✅ 自动重启机制正常
- ✅ 错误处理完善

### 主要提交
- `9ed3037` - feat: auto device re-register + restart on ILLEGAL_ACCESS/GZIP errors
- `c363a80` - fix(fullbook): real chapter count from directory; null-safe maxChapters
- `e6a9430` - fix(fullbook): use real directory size for totalChapters
- `6905035` - feat: cache export merge script defaults; add cache APIs
- `f019044` - feat: add full-book download + Redis cache view APIs

## 🚀 部署说明

### 启动服务
```bash
# 编译项目
mvn -q -DskipTests package

# 启动服务
java -jar target/unidbg-boot-server-0.0.1-SNAPSHOT.jar
```

### 服务管理
```bash
# 停止服务
pkill -f "java.*unidbg"

# 查看日志
tail -f target/spring-boot.log
```

## 🔍 故障排除

### 常见问题
1. **API返回空响应**: 检查设备配置和网络连接
2. **服务启动失败**: 检查端口占用和依赖配置
3. **设备注册失败**: 检查Redis连接和权限

### 日志查看
```bash
# 查看最新日志
tail -n 50 target/spring-boot.log

# 搜索错误信息
grep -i error target/spring-boot.log
```

## 📝 开发说明

### 代码更新
当前分支 `1013new` 可以正常进行代码更新：
- 直接修改代码并提交
- 创建功能分支开发
- 合并其他分支的更新

### 注意事项
- 修改核心功能前建议先测试
- 保持向后兼容性
- 及时更新文档

---

**最后更新**: 2025-10-13  
**维护者**: zhangyuming  
**状态**: 生产就绪 ✅
