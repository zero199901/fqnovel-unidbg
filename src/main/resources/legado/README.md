# FQNovel Legado 书源配置

本目录包含用于 @gedoor/legado 阅读3 的 FQNovel API 书源配置文件。

## 书源文件

### 1. fqnovel-booksource.json
- **名称**: FQNovel API
- **类型**: 标准书源
- **功能**: 支持书籍搜索、详情获取、目录浏览、单章节内容获取
- **特点**: 稳定可靠，适合日常使用

### 2. fqnovel-batch-booksource.json  
- **名称**: FQNovel 批量API
- **类型**: 批量书源
- **功能**: 支持批量获取章节内容，提高阅读体验
- **特点**: 性能更好，适合连续阅读

## 使用方法

### 1. 启动 FQNovel API 服务
```bash
# 确保服务在 localhost:9999 运行
java -jar target/unidbg-boot-server-0.0.1-SNAPSHOT.jar
```

### 2. 导入书源到 Legado
1. 打开 Legado 阅读 APP
2. 进入「书源管理」
3. 选择「导入书源」 
4. 复制对应的 JSON 配置文件内容
5. 粘贴并导入

### 3. 书源配置说明

#### API 端点映射
- **搜索**: `/api/fqsearch/books` 
- **书籍详情**: `/api/fqnovel/book/{bookId}`
- **书籍目录**: `/api/fqsearch/directory/{bookId}`
- **章节内容**: `/api/fqnovel/item_id/{itemId}` 或 `/api/fqnovel/chapter/{bookId}/{chapterId}`
- **批量章节**: `/api/fqnovel/chapters/batch`

#### 关键参数
- `bookId`: 书籍唯一标识
- `itemId`: 章节唯一标识  
- `chapterRange`: 章节范围 (如 "1-30")
- `chapterIds`: 章节 ID 列表 (如 [
  "7271262165057667646",
  "7271262274424144446"
  ])
- `query`: 搜索关键词
- `offset`: 分页偏移量
- `count`: 每页数量

## 配置自定义

### 修改服务地址
如果 FQNovel API 服务部署在其他地址，需要修改以下字段:
- `bookSourceUrl`
- `exploreUrl` 
- `searchUrl`
- `ruleBookInfo.tocUrl`
- `ruleExplore.bookUrl`
- `ruleSearch.bookUrl`