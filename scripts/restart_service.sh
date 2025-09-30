#!/bin/bash

# 简单的服务重启脚本
# 用于自动重启FQNovel服务

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/restart_service.log"
SERVICE_URL="http://127.0.0.1:9091/api/fqnovel/health"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查服务是否运行
is_service_running() {
    # 检查Java进程
    if ! pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        return 1
    fi
    
    # 检查HTTP响应
    local response=$(curl -s -w "%{http_code}" -o /dev/null "$SERVICE_URL" 2>/dev/null)
    if [ "$response" = "200" ]; then
        return 0
    else
        return 1
    fi
}

# 停止服务
stop_service() {
    log "停止服务..."
    pkill -f "java.*UnidbgServerApplication" 2>/dev/null
    sleep 3
    
    # 强制停止
    if pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        log "强制停止服务..."
        pkill -9 -f "java.*UnidbgServerApplication" 2>/dev/null
        sleep 2
    fi
}

# 启动服务
start_service() {
    log "启动服务..."
    cd "$PROJECT_DIR"
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    
    # 等待启动
    log "等待服务启动..."
    for i in {1..30}; do
        sleep 2
        if is_service_running; then
            log "✅ 服务启动成功"
            return 0
        fi
    done
    
    log "❌ 服务启动失败"
    return 1
}

# 重启服务
restart_service() {
    log "🔄 重启服务..."
    stop_service
    start_service
}

# 主函数
main() {
    log "🚀 服务管理脚本启动"
    
    case "${1:-status}" in
        "start")
            if is_service_running; then
                log "服务已在运行"
            else
                start_service
            fi
            ;;
        "stop")
            stop_service
            ;;
        "restart")
            restart_service
            ;;
        "status")
            if is_service_running; then
                log "✅ 服务运行正常"
            else
                log "❌ 服务未运行"
            fi
            ;;
        "monitor")
            log "开始监控模式..."
            while true; do
                if ! is_service_running; then
                    log "检测到服务异常，正在重启..."
                    restart_service
                fi
                sleep 15
            done
            ;;
        *)
            echo "用法: $0 {start|stop|restart|status|monitor}"
            echo "  start   - 启动服务"
            echo "  stop    - 停止服务"
            echo "  restart - 重启服务"
            echo "  status  - 检查状态"
            echo "  monitor - 监控模式（自动重启）"
            ;;
    esac
}

main "$@"
