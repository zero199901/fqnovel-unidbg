#!/bin/bash

# FQNovel服务管理脚本
# 用于启动、停止、重启和监控服务

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
SERVICE_URL="http://127.0.0.1:9091/api/fqnovel/health"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

# 检查服务状态
check_status() {
    local response=$(curl -s -w "%{http_code}" -o /dev/null "$SERVICE_URL" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✅ 服务运行正常${NC}"
        return 0
    else
        echo -e "${RED}❌ 服务无响应${NC}"
        return 1
    fi
}

# 启动服务
start_service() {
    log "正在启动FQNovel服务..."
    
    # 检查是否已经在运行
    if pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        echo -e "${YELLOW}⚠️  服务已在运行中${NC}"
        return 0
    fi
    
    cd "$PROJECT_DIR"
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    
    # 等待服务启动
    log "等待服务启动..."
    for i in {1..30}; do
        sleep 2
        if check_status > /dev/null 2>&1; then
            echo -e "${GREEN}✅ 服务启动成功${NC}"
            return 0
        fi
    done
    
    echo -e "${RED}❌ 服务启动失败${NC}"
    return 1
}

# 停止服务
stop_service() {
    log "正在停止FQNovel服务..."
    
    if ! pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        echo -e "${YELLOW}⚠️  服务未运行${NC}"
        return 0
    fi
    
    pkill -f "java.*UnidbgServerApplication" 2>/dev/null
    sleep 5
    
    # 强制停止
    if pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        log "强制停止服务..."
        pkill -9 -f "java.*UnidbgServerApplication" 2>/dev/null
        sleep 3
    fi
    
    echo -e "${GREEN}✅ 服务已停止${NC}"
}

# 重启服务
restart_service() {
    log "正在重启FQNovel服务..."
    stop_service
    start_service
}

# 启动监控
start_monitor() {
    log "正在启动错误监控..."
    
    if pgrep -f "monitor_errors.sh" > /dev/null; then
        echo -e "${YELLOW}⚠️  监控已在运行中${NC}"
        return 0
    fi
    
    cd "$PROJECT_DIR"
    nohup ./monitor_errors.sh > monitor.log 2>&1 &
    
    sleep 2
    if pgrep -f "monitor_errors.sh" > /dev/null; then
        echo -e "${GREEN}✅ 监控启动成功${NC}"
    else
        echo -e "${RED}❌ 监控启动失败${NC}"
    fi
}

# 停止监控
stop_monitor() {
    log "正在停止错误监控..."
    pkill -f "monitor_errors.sh" 2>/dev/null
    echo -e "${GREEN}✅ 监控已停止${NC}"
}

# 查看日志
view_logs() {
    local log_type="$1"
    
    case "$log_type" in
        "app")
            echo -e "${BLUE}=== 应用日志 ===${NC}"
            tail -20 "$PROJECT_DIR/run.log" 2>/dev/null || echo "日志文件不存在"
            ;;
        "monitor")
            echo -e "${BLUE}=== 监控日志 ===${NC}"
            tail -20 "$PROJECT_DIR/monitor.log" 2>/dev/null || echo "监控日志不存在"
            ;;
        "auto")
            echo -e "${BLUE}=== 自动重启日志 ===${NC}"
            tail -20 "$PROJECT_DIR/auto_restart.log" 2>/dev/null || echo "自动重启日志不存在"
            ;;
        *)
            echo -e "${BLUE}=== 应用日志 ===${NC}"
            tail -10 "$PROJECT_DIR/run.log" 2>/dev/null || echo "日志文件不存在"
            echo -e "\n${BLUE}=== 监控日志 ===${NC}"
            tail -10 "$PROJECT_DIR/monitor.log" 2>/dev/null || echo "监控日志不存在"
            ;;
    esac
}

# 启动监控系统
start_monitoring() {
    log "启动监控系统..."
    
    # 启动智能监控脚本
    if ! pgrep -f "smart_monitor.sh" > /dev/null; then
        log "启动智能监控脚本..."
        cd "$PROJECT_DIR"
        nohup ./smart_monitor.sh > /dev/null 2>&1 &
        sleep 2
    fi
    
    # 启动守护进程监控
    if ! pgrep -f "daemon_monitor.sh" > /dev/null; then
        log "启动守护进程监控..."
        cd "$PROJECT_DIR"
        nohup ./daemon_monitor.sh > /dev/null 2>&1 &
        sleep 2
    fi
    
    # 检查监控状态
    if pgrep -f "smart_monitor.sh" > /dev/null && pgrep -f "daemon_monitor.sh" > /dev/null; then
        echo -e "${GREEN}✅ 监控系统启动成功${NC}"
    else
        echo -e "${RED}❌ 监控系统启动失败${NC}"
        return 1
    fi
}

# 停止监控系统
stop_monitoring() {
    log "停止监控系统..."
    
    # 停止智能监控脚本
    if pgrep -f "smart_monitor.sh" > /dev/null; then
        log "停止智能监控脚本..."
        pkill -f "smart_monitor.sh"
    fi
    
    # 停止守护进程监控
    if pgrep -f "daemon_monitor.sh" > /dev/null; then
        log "停止守护进程监控..."
        pkill -f "daemon_monitor.sh"
    fi
    
    # 停止自动重启脚本
    if pgrep -f "auto_restart.sh" > /dev/null; then
        log "停止自动重启脚本..."
        pkill -f "auto_restart.sh"
    fi
    
    echo -e "${GREEN}✅ 监控系统已停止${NC}"
}

# 检查监控状态
check_monitoring() {
    echo "监控系统状态:"
    echo "=================="
    
    if pgrep -f "smart_monitor.sh" > /dev/null; then
        echo -e "智能监控脚本: ${GREEN}✅ 运行中${NC}"
    else
        echo -e "智能监控脚本: ${RED}❌ 未运行${NC}"
    fi
    
    if pgrep -f "daemon_monitor.sh" > /dev/null; then
        echo -e "守护进程监控: ${GREEN}✅ 运行中${NC}"
    else
        echo -e "守护进程监控: ${RED}❌ 未运行${NC}"
    fi
    
    if pgrep -f "auto_restart.sh" > /dev/null; then
        echo -e "自动重启脚本: ${GREEN}✅ 运行中${NC}"
    else
        echo -e "自动重启脚本: ${RED}❌ 未运行${NC}"
    fi
    
    echo ""
}

# 测试自动重启机制
test_restart() {
    log "测试自动重启机制..."
    
    if [ -f "$PROJECT_DIR/test_restart.sh" ]; then
        cd "$PROJECT_DIR"
        ./test_restart.sh
    else
        echo -e "${RED}❌ 测试脚本不存在${NC}"
        return 1
    fi
}

# 显示帮助
show_help() {
    echo -e "${BLUE}FQNovel服务管理脚本${NC}"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start        启动服务"
    echo "  stop         停止服务"
    echo "  restart      重启服务"
    echo "  status       查看服务状态"
    echo "  monitor      启动错误监控"
    echo "  stop-monitor 停止错误监控"
    echo "  start-monitor 启动监控系统"
    echo "  stop-monitoring 停止监控系统"
    echo "  check-monitor 检查监控状态"
    echo "  test-restart  测试自动重启机制"
    echo "  logs         查看日志"
    echo "  logs app     查看应用日志"
    echo "  logs monitor 查看监控日志"
    echo "  help         显示帮助"
    echo ""
}

# 主函数
main() {
    case "$1" in
        "start")
            start_service
            ;;
        "stop")
            stop_service
            ;;
        "restart")
            restart_service
            ;;
        "status")
            check_status
            ;;
        "monitor")
            start_monitor
            ;;
        "stop-monitor")
            stop_monitor
            ;;
        "start-monitor")
            start_monitoring
            ;;
        "stop-monitoring")
            stop_monitoring
            ;;
        "check-monitor")
            check_monitoring
            ;;
        "test-restart")
            test_restart
            ;;
        "logs")
            view_logs "$2"
            ;;
        "help"|"--help"|"-h")
            show_help
            ;;
        "")
            echo -e "${BLUE}FQNovel服务状态:${NC}"
            check_status
            echo ""
            echo -e "${BLUE}进程状态:${NC}"
            if pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
                echo -e "${GREEN}✅ 应用进程运行中${NC}"
            else
                echo -e "${RED}❌ 应用进程未运行${NC}"
            fi
            
            if pgrep -f "monitor_errors.sh" > /dev/null; then
                echo -e "${GREEN}✅ 监控进程运行中${NC}"
            else
                echo -e "${YELLOW}⚠️  监控进程未运行${NC}"
            fi
            ;;
        *)
            echo -e "${RED}❌ 未知命令: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
