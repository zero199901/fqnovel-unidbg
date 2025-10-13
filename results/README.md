# 设备注册结果目录结构

本目录包含批量设备注册工具生成的所有文件，按功能分类存储。

## 目录结构

```
results/
├── raw_data/          # 原始注册数据
│   └── device_register_full_*.json
├── configs/           # 批量配置文件
│   └── device_register_xml_configs_*.yaml
├── individual/        # 单独设备配置文件
│   ├── device_001_品牌_型号_时间戳.yaml
│   ├── device_002_品牌_型号_时间戳.yaml
│   └── ...
└── reports/           # 报告和摘要文件
    ├── device_register_report_*.md
    └── device_summary_*.yaml
```

## 文件说明

### 📁 raw_data/
- **device_register_full_*.json**: 完整的设备注册原始数据
- 包含所有请求和响应的详细信息
- 用于调试和分析注册过程

### 📁 configs/
- **device_register_xml_configs_*.yaml**: 批量设备配置集合
- 包含所有成功注册设备的XML格式配置
- 可直接用于批量部署

### 📁 individual/
- **device_XXX_品牌_型号_时间戳.yaml**: 单个设备配置文件
- 文件名格式：`device_序号_品牌_型号_时间戳.yaml`
- 每个文件包含一个设备的完整配置信息
- 便于单独使用和管理

### 📁 reports/
- **device_register_report_*.md**: 设备注册报告
  - 注册统计信息
  - 设备品牌分布
  - Android版本分布
  - 详细设备信息
- **device_summary_*.yaml**: 设备信息摘要
  - 结构化的设备信息汇总
  - 便于程序化处理

## 使用建议

1. **开发调试**: 查看 `raw_data/` 中的原始数据
2. **批量部署**: 使用 `configs/` 中的批量配置文件
3. **单独使用**: 从 `individual/` 中选择特定设备配置
4. **统计分析**: 查看 `reports/` 中的报告和摘要

## 文件命名规则

- 时间戳格式：`YYYYMMDD_HHMMSS`
- 设备序号：3位数字，如 `001`, `002`, `003`
- 品牌和型号：使用下划线替换空格和特殊字符
- 文件扩展名：`.json` (原始数据), `.yaml` (配置), `.md` (报告)
