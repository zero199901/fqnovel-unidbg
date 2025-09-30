#!/bin/bash

# 错误监控脚本
# 专门监控ILLEGAL_ACCESS错误并自动重启服务

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/run.log"
MONITOR_LOG="$PROJECT_DIR/monitor.log"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$MONITOR_LOG"
}

# 重启服务
restart_service() {
    log "检测到ILLEGAL_ACCESS错误，正在重启服务..."
    
    # 停止服务
    pkill -f "java.*UnidbgServerApplication" 2>/dev/null
    sleep 5
    
    # 启动服务
    cd "$PROJECT_DIR"
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    
    log "服务重启完成"
}

# 监控日志文件
monitor_log() {
    log "开始监控错误日志..."
    
    # 使用tail -f监控日志文件
    tail -f "$LOG_FILE" | while read line; do
        if echo "$line" | grep -q "ILLEGAL_ACCESS"; then
            log "检测到ILLEGAL_ACCESS错误: $line"
            restart_service
        fi
    done
}

# 主函数
main() {
    log "错误监控脚本启动"
    
    if [ ! -f "$LOG_FILE" ]; then
        log "错误: 日志文件不存在: $LOG_FILE"
        exit 1
    fi
    
    monitor_log
}

# 处理信号
cleanup() {
    log "监控脚本停止"
    exit 0
}

trap cleanup SIGINT SIGTERM

main "$@"
