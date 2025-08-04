# FQNovel API Documentation

## 概述

FQNovel API 提供小说章节内容获取功能，使用 AES-128-CBC 加密和 GZIP 压缩。实现包括：

- **加密解密支持**: 基于 AES-128-CBC 的加密/解密工具类
- **批量章节获取**: 支持一次性获取多个章节内容
- **自动解密解压**: 自动处理加密和压缩的章节内容
- **FQ 签名集成**: 与现有的 FQ 签名系统集成

## API 端点

### 1. 健康检查
```
GET /api/fqnovel/health
```

响应示例：
```json
{
  "status": "UP",
  "service": "FQNovel Service",
  "timestamp": 1672531200000
}
```

### 2. 获取单个章节内容 (新增)
```
GET /api/fqnovel/item_id/{itemId}
```

参数：
- `itemId`: 章节ID

响应：直接返回解密后的章节文本内容

示例：
```bash
curl "http://localhost:9999/api/fqnovel/item_id/7282975997584998953"
```

### 3. 获取章节内容 (兼容接口)
```
GET /api/fqnovel/chapter/{bookId}/{chapterId}
POST /api/fqnovel/chapter
```

这些接口保持向后兼容，但内部使用新的 API 模式。

### 4. 批量获取章节内容 (更新)
```
POST /api/fqnovel/chapters/batch
```

请求体：
```json
{
  "bookId": "7276384138653862966",
  "chapterIds": ["7282975997584998953", "7282975997584998954"]
}
```

### 5. 批量获取章节内容 (新增 - 支持范围)
```
POST /api/fqnovel/chapters/batch
```

请求体：
```json
{
  "bookId": "7276384138653862966",
  "chapterRange": "1-30"
}
```

参数：
- `bookId`: 书籍ID (必须)
- `chapterRange`: 章节范围，支持格式：
  - 范围格式: "1-30" (获取章节1到30)
  - 单个章节: "5" (只获取章节5)
  - 最少1章，最多30章

响应：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "bookId": "7276384138653862966",
    "bookInfo": {
      "bookId": "7276384138653862966",
      "bookName": ""
    },
    "chapters": {
      "1": {
        "chapterName": "第一章 标题",
        "rawContent": "<html>原始HTML内容</html>",
        "txtContent": "纯文本内容",
        "wordCount": 1500,
        "isFree": true
      },
      "2": {
        "chapterName": "第二章 标题",
        "rawContent": "<html>原始HTML内容</html>",
        "txtContent": "纯文本内容",
        "wordCount": 1600,
        "isFree": true
      }
    },
    "requestedRange": "1-30",
    "successCount": 2,
    "totalRequested": 30
  },
  "serverTime": 1672531200000
}
```

## 技术实现

### 核心组件

#### 1. FqCrypto 加密工具类
```java
// 创建加密器
FqCrypto crypto = new FqCrypto(hexKey);

// 解密内容
byte[] decrypted = crypto.decrypt(encryptedData);

// 生成注册密钥内容
String content = crypto.newRegisterKeyContent(deviceId, "0");
```

#### 2. FqVariable 配置类
```java
FqVariable var = new FqVariable();
// installId: "933935730456617"
// serverDeviceId: "933935730452521" 
// aid: "1967"
// updateVersionCode: "68132"
```

#### 3. API 流程
1. 使用 `batch_full` API 获取加密内容
2. 调用 `register_key` API 获取解密密钥
3. 使用 AES-128-CBC 解密内容
4. 使用 GZIP 解压缩最终内容

### API 端点配置

- **基础URL**: `https://api5-normal-sinfonlineb.fqnovel.com`
- **批量获取**: `/reading/reader/batch_full/v`
- **注册密钥**: `/reading/crypt/registerkey`

### 参数说明

#### batch_full 参数
- `item_ids`: 章节ID列表，逗号分隔
- `req_type`: 请求类型 (0=下载, 1=在线阅读)
- `aid`: 应用ID
- `update_version_code`: 版本代码

#### 请求头
- `Cookie`: `install_id={installId}`
- `accept`: `application/json; charset=utf-8,application/x-protobuf`
- `user-agent`: 客户端标识
- `accept-encoding`: `gzip`

## 使用示例

### 获取书籍信息
```bash
curl "http://localhost:9999/api/fqnovel/book/7276384138653862966"
```

### 获取单个章节
```bash
curl "http://localhost:9999/api/fqnovel/item_id/7282975997584998953"
```

### 批量获取章节
```bash
curl -X POST "http://localhost:9999/api/fqnovel/chapters/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "7276384138653862966",
    "chapterIds": ["7282975997584998953", "7282975997584998954"]
  }'
```

### Legado 阅读器书源支持
本项目已支持 @gedoor/legado 阅读3，提供完整的书源配置文件：
- 书源：`src/main/resources/legado/fqnovel.json`

详细配置说明请参考：`src/main/resources/legado/README.md`

## 配置

### 默认配置
```java
// 在 FqVariable 中设置
private String installId = "933935730456617";
private String serverDeviceId = "933935730452521";
private String aid = "1967";
private String updateVersionCode = "68132";
```

### 加密密钥
```java
public static final String REG_KEY = "ac25c67ddd8f38c1b37a2348828e222e";
```

## 错误处理

所有 API 都返回统一的错误格式：
```json
{
  "code": -1,
  "message": "错误描述",
  "data": null,
  "serverTime": 1672531200000
}
```

## 安全说明

- 所有 API 请求都会通过现有的 FQ 签名系统进行签名
- 章节内容使用 AES-128-CBC 加密传输
- 支持 GZIP 压缩以减少传输大小

## 依赖

- Spring Boot Web
- Jackson JSON
- Java Crypto API
- 现有的 FQEncryptServiceWorker