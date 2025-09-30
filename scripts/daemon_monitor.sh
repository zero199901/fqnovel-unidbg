#!/bin/bash

# 守护进程监控脚本
# 确保自动重启脚本持续运行

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/daemon_monitor.log"
AUTO_RESTART_SCRIPT="$PROJECT_DIR/auto_restart.sh"
CHECK_INTERVAL=30

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查自动重启脚本是否运行
check_auto_restart() {
    if pgrep -f "auto_restart.sh" > /dev/null; then
        return 0
    else
        return 1
    fi
}

# 启动自动重启脚本
start_auto_restart() {
    log "启动自动重启脚本..."
    cd "$PROJECT_DIR"
    nohup "$AUTO_RESTART_SCRIPT" > /dev/null 2>&1 &
    sleep 3
    
    if check_auto_restart; then
        log "自动重启脚本启动成功"
        return 0
    else
        log "自动重启脚本启动失败"
        return 1
    fi
}

# 主监控循环
monitor_daemon() {
    log "守护进程监控启动"
    
    while true; do
        if ! check_auto_restart; then
            log "检测到自动重启脚本未运行，正在重新启动..."
            start_auto_restart
        fi
        
        sleep "$CHECK_INTERVAL"
    done
}

# 处理信号
cleanup() {
    log "守护进程收到停止信号，正在清理..."
    exit 0
}

# 设置信号处理
trap cleanup SIGINT SIGTERM

# 主函数
main() {
    log "FQNovel守护进程监控启动"
    
    # 检查项目目录
    if [ ! -d "$PROJECT_DIR" ]; then
        log "错误: 项目目录不存在: $PROJECT_DIR"
        exit 1
    fi
    
    # 检查自动重启脚本是否存在
    if [ ! -f "$AUTO_RESTART_SCRIPT" ]; then
        log "错误: 自动重启脚本不存在: $AUTO_RESTART_SCRIPT"
        exit 1
    fi
    
    # 确保自动重启脚本可执行
    chmod +x "$AUTO_RESTART_SCRIPT"
    
    # 如果自动重启脚本没有运行，先启动它
    if ! check_auto_restart; then
        start_auto_restart
    else
        log "自动重启脚本已在运行"
    fi
    
    # 开始监控
    monitor_daemon
}

# 运行主函数
main "$@"
