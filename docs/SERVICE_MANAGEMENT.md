# FQNovel服务管理指南

## 概述

本项目提供了自动重启和监控功能，当检测到ILLEGAL_ACCESS错误时会自动重启服务。

## 管理脚本

### 主要管理脚本: `manage_service.sh`

这是主要的服务管理脚本，提供了完整的服务控制功能。

#### 使用方法

```bash
# 查看服务状态
./manage_service.sh

# 启动服务
./manage_service.sh start

# 停止服务
./manage_service.sh stop

# 重启服务
./manage_service.sh restart

# 查看服务状态
./manage_service.sh status

# 启动错误监控
./manage_service.sh monitor

# 停止错误监控
./manage_service.sh stop-monitor

# 查看日志
./manage_service.sh logs

# 查看应用日志
./manage_service.sh logs app

# 查看监控日志
./manage_service.sh logs monitor

# 显示帮助
./manage_service.sh help
```

### 监控脚本: `monitor_errors.sh`

专门监控ILLEGAL_ACCESS错误并自动重启服务的脚本。

#### 功能
- 实时监控 `run.log` 文件
- 检测到 `ILLEGAL_ACCESS` 错误时自动重启服务
- 记录监控日志到 `monitor.log`

#### 使用方法
```bash
# 启动监控（后台运行）
nohup ./monitor_errors.sh > monitor.log 2>&1 &

# 停止监控
pkill -f monitor_errors.sh
```

### 自动重启脚本: `auto_restart.sh`

更全面的监控脚本，包含多种检查机制。

#### 功能
- 定期检查服务健康状态
- 监控日志错误
- 自动重启服务
- 记录详细日志

## 快速开始

### 1. 启动服务
```bash
./manage_service.sh start
```

### 2. 启动监控
```bash
./manage_service.sh monitor
```

### 3. 检查状态
```bash
./manage_service.sh status
```

## 日志文件

- `run.log` - 应用运行日志
- `monitor.log` - 监控脚本日志
- `auto_restart.log` - 自动重启脚本日志

## 故障排除

### 服务无法启动
1. 检查端口9091是否被占用
2. 查看 `run.log` 中的错误信息
3. 确保Java环境正确配置

### 监控不工作
1. 检查监控进程是否运行: `ps aux | grep monitor_errors`
2. 查看 `monitor.log` 中的错误信息
3. 确保 `run.log` 文件存在

### 频繁重启
1. 检查网络连接
2. 查看API配置是否正确
3. 检查认证参数是否有效

## API接口

服务启动后，可以通过以下接口访问：

- **健康检查**: `GET http://127.0.0.1:9091/api/fqnovel/health`
- **批量章节**: `POST http://127.0.0.1:9091/api/fqnovel/chapters/batch`
- **单个章节**: `GET http://127.0.0.1:9091/api/fqnovel/chapter/{bookId}/{chapterId}`
- **书籍信息**: `GET http://127.0.0.1:9091/api/fqnovel/book/{bookId}`

## 注意事项

1. 确保脚本有执行权限: `chmod +x *.sh`
2. 监控脚本会持续运行，需要手动停止
3. 服务重启会中断正在进行的请求
4. 建议在生产环境中使用进程管理工具如systemd或supervisor

## 示例用法

```bash
# 完整的服务启动流程
./manage_service.sh start
./manage_service.sh monitor
./manage_service.sh status

# 查看日志
./manage_service.sh logs

# 重启服务
./manage_service.sh restart

# 停止所有服务
./manage_service.sh stop
./manage_service.sh stop-monitor
```
