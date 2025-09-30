# FQNovel 自动重启指南

## 概述

现在项目已经配置了可靠的自动重启功能，当服务出现问题时会自动重启。

## 使用方法

### 主要脚本: `restart_service.sh`

这是一个简单可靠的服务管理脚本，提供以下功能：

```bash
# 启动服务
./restart_service.sh start

# 停止服务
./restart_service.sh stop

# 重启服务
./restart_service.sh restart

# 检查服务状态
./restart_service.sh status

# 启动监控模式（自动重启）
./restart_service.sh monitor
```

### 自动重启功能

**启动监控模式：**
```bash
# 前台运行（可以看到日志）
./restart_service.sh monitor

# 后台运行
nohup ./restart_service.sh monitor > /dev/null 2>&1 &
```

**监控功能：**
- 每15秒检查一次服务状态
- 检测Java进程是否存在
- 检测HTTP健康检查是否正常
- 发现异常时自动重启服务
- 记录所有操作到 `restart_service.log`

## 测试结果

✅ **自动重启测试成功**
- 手动停止服务后，监控脚本在10秒内检测到异常
- 自动重启服务成功
- API功能正常工作

## 日志文件

- `restart_service.log` - 服务管理日志
- 包含启动、停止、重启等所有操作记录

## 当前状态

- ✅ 服务运行正常 (端口9091)
- ✅ 自动监控已启动
- ✅ API功能正常
- ✅ 自动重启机制已验证

## 故障排除

如果遇到问题：

1. **检查服务状态：**
   ```bash
   ./restart_service.sh status
   ```

2. **查看日志：**
   ```bash
   tail -f restart_service.log
   ```

3. **手动重启：**
   ```bash
   ./restart_service.sh restart
   ```

4. **停止监控：**
   ```bash
   pkill -f restart_service.sh
   ```

## 注意事项

- 监控脚本会持续运行，每15秒检查一次
- 如果服务连续重启失败，需要手动检查问题
- 日志文件会持续增长，定期清理
- 确保端口9091没有被其他程序占用
