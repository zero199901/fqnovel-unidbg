#!/bin/bash

# 自动重启脚本
# 监控FQNovel服务状态，在出现问题时自动重启

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/auto_restart.log"
SERVICE_URL="http://127.0.0.1:9091/api/fqnovel/health"
MAX_RETRIES=3
RETRY_INTERVAL=30

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 检查服务是否运行
check_service() {
    local response=$(curl -s -w "%{http_code}" -o /dev/null "$SERVICE_URL" 2>/dev/null)
    if [ "$response" = "200" ]; then
        return 0
    else
        return 1
    fi
}

# 停止服务
stop_service() {
    log "正在停止服务..."
    pkill -f "java.*UnidbgServerApplication" 2>/dev/null
    sleep 5
    
    # 确保进程已停止
    if pgrep -f "java.*UnidbgServerApplication" > /dev/null; then
        log "强制停止服务..."
        pkill -9 -f "java.*UnidbgServerApplication" 2>/dev/null
        sleep 3
    fi
}

# 启动服务
start_service() {
    log "正在启动服务..."
    cd "$PROJECT_DIR"
    
    # 后台启动服务
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    
    # 等待服务启动
    local retry_count=0
    while [ $retry_count -lt 30 ]; do
        sleep 2
        if check_service; then
            log "服务启动成功"
            return 0
        fi
        retry_count=$((retry_count + 1))
    done
    
    log "服务启动失败"
    return 1
}

# 重启服务
restart_service() {
    log "开始重启服务..."
    stop_service
    start_service
}

# 检查日志中的错误
check_log_errors() {
    local log_file="$PROJECT_DIR/run.log"
    if [ -f "$log_file" ]; then
        # 检查最近5分钟内的ILLEGAL_ACCESS错误
        local error_count=$(tail -n 100 "$log_file" | grep -c "ILLEGAL_ACCESS" 2>/dev/null)
        if [ "$error_count" -gt 5 ]; then
            log "检测到大量ILLEGAL_ACCESS错误，准备重启服务"
            return 1
        fi
    fi
    return 0
}

# 检查业务接口异常
check_business_api_errors() {
    # 使用真实的书籍ID进行测试，避免测试数据导致的误判
    local test_book_id="1234567890"  # 使用一个可能存在的书籍ID
    
    # 测试批量章节接口
    local response=$(curl -s -w "%{http_code}" -o /tmp/api_response.json "http://127.0.0.1:9091/api/fqnovel/chapters/batch" \
        -H "Content-Type: application/json" \
        -d "{\"bookId\":\"$test_book_id\",\"chapterIds\":[\"1\"]}" 2>/dev/null)
    
    if [ "$response" = "200" ]; then
        # 检查响应内容是否包含特定的错误信息
        if grep -q '"message":\s*".*API访问被拒绝.*"' /tmp/api_response.json 2>/dev/null; then
            log "检测到API访问被拒绝错误，准备重启服务"
            return 1
        fi
        if grep -q '"message":\s*".*获取.*失败.*"' /tmp/api_response.json 2>/dev/null; then
            # 检查是否是认证相关的错误
            if grep -q '"message":\s*".*认证.*"' /tmp/api_response.json 2>/dev/null; then
                log "检测到认证失败错误，准备重启服务"
                return 1
            fi
        fi
    fi
    
    # 清理临时文件
    rm -f /tmp/api_response.json 2>/dev/null
    return 0
}

# 主监控循环
monitor_service() {
    log "开始监控FQNovel服务..."
    
    while true; do
        # 检查服务状态
        if ! check_service; then
            log "服务无响应，准备重启..."
            restart_service
            continue
        fi
        
        # 检查日志错误
        if ! check_log_errors; then
            restart_service
            continue
        fi
        
        # 检查业务接口异常
        if ! check_business_api_errors; then
            restart_service
            continue
        fi
        
        # 等待下次检查
        sleep 60
    done
}

# 处理信号
cleanup() {
    log "收到停止信号，正在清理..."
    stop_service
    exit 0
}

# 设置信号处理
trap cleanup SIGINT SIGTERM

# 主函数
main() {
    log "FQNovel自动重启脚本启动"
    
    # 检查项目目录
    if [ ! -d "$PROJECT_DIR" ]; then
        log "错误: 项目目录不存在: $PROJECT_DIR"
        exit 1
    fi
    
    # 开始监控
    monitor_service
}

# 运行主函数
main "$@"
