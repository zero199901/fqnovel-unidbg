#!/bin/bash

# 测试自动重启机制
# 模拟服务异常情况

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/test_restart.log"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查服务状态
check_service() {
    local response=$(curl -s -w "%{http_code}" -o /dev/null http://127.0.0.1:9091/api/fqnovel/health 2>/dev/null)
    if [ "$response" = "200" ]; then
        return 0
    else
        return 1
    fi
}

# 测试自动重启机制
test_restart_mechanism() {
    log "开始测试自动重启机制..."
    
    # 1. 检查服务是否正常运行
    if check_service; then
        log "✓ 服务正常运行"
    else
        log "✗ 服务异常"
        return 1
    fi
    
    # 2. 检查自动重启脚本是否运行
    if pgrep -f "auto_restart.sh" > /dev/null; then
        log "✓ 自动重启脚本正在运行"
    else
        log "✗ 自动重启脚本未运行"
        return 1
    fi
    
    # 3. 检查守护进程是否运行
    if pgrep -f "daemon_monitor.sh" > /dev/null; then
        log "✓ 守护进程正在运行"
    else
        log "✗ 守护进程未运行"
        return 1
    fi
    
    # 4. 模拟服务异常（停止服务）
    log "模拟服务异常 - 停止服务..."
    pkill -f "java.*UnidbgServerApplication"
    
    # 5. 等待自动重启
    log "等待自动重启机制工作..."
    local retry_count=0
    while [ $retry_count -lt 30 ]; do
        sleep 2
        if check_service; then
            log "✓ 服务已自动重启并恢复正常"
            return 0
        fi
        retry_count=$((retry_count + 1))
    done
    
    log "✗ 自动重启失败"
    return 1
}

# 主函数
main() {
    log "FQNovel自动重启机制测试开始"
    
    if test_restart_mechanism; then
        log "✓ 自动重启机制测试通过"
        exit 0
    else
        log "✗ 自动重启机制测试失败"
        exit 1
    fi
}

# 运行主函数
main "$@"
