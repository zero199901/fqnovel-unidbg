#!/bin/bash

# 智能监控脚本
# 专门监控特定的业务接口异常，避免误判

PROJECT_DIR="/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
LOG_FILE="$PROJECT_DIR/smart_monitor.log"
SERVICE_URL="http://127.0.0.1:9091/api/fqnovel/health"
BATCH_API_URL="http://127.0.0.1:9091/api/fqnovel/chapters/batch"
CHECK_INTERVAL=300  # 5分钟检查一次
ERROR_THRESHOLD=3   # 连续3次错误才重启
ERROR_COUNT_FILE="/tmp/fqnovel_error_count"

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

# 检查特定的业务接口异常
check_specific_errors() {
    # 使用一个真实的书籍ID进行测试
    local test_book_id="1234567890"
    
    # 测试批量章节接口
    local response=$(curl -s -w "%{http_code}" -o /tmp/api_response.json "$BATCH_API_URL" \
        -H "Content-Type: application/json" \
        -d "{\"bookId\":\"$test_book_id\",\"chapterIds\":[\"1\"]}" 2>/dev/null)
    
    if [ "$response" = "200" ]; then
        # 检查是否包含特定的错误信息
        if grep -q '"message":\s*".*API访问被拒绝.*"' /tmp/api_response.json 2>/dev/null; then
            log "检测到API访问被拒绝错误"
            return 1
        fi
        if grep -q '"message":\s*".*获取.*失败.*"' /tmp/api_response.json 2>/dev/null; then
            # 检查是否是认证相关的错误
            if grep -q '"message":\s*".*认证.*"' /tmp/api_response.json 2>/dev/null; then
                log "检测到认证失败错误"
                return 1
            fi
        fi
    fi
    
    # 清理临时文件
    rm -f /tmp/api_response.json 2>/dev/null
    return 0
}

# 更新错误计数
update_error_count() {
    local count=0
    if [ -f "$ERROR_COUNT_FILE" ]; then
        count=$(cat "$ERROR_COUNT_FILE")
    fi
    count=$((count + 1))
    echo "$count" > "$ERROR_COUNT_FILE"
    echo "$count"
}

# 重置错误计数
reset_error_count() {
    rm -f "$ERROR_COUNT_FILE" 2>/dev/null
}

# 主监控循环
monitor_service() {
    log "智能监控脚本启动"
    
    while true; do
        # 检查服务状态
        if ! check_service; then
            log "服务无响应，准备重启..."
            restart_service
            reset_error_count
            continue
        fi
        
        # 检查特定的业务接口异常
        if ! check_specific_errors; then
            local error_count=$(update_error_count)
            log "检测到业务接口异常，错误计数: $error_count"
            
            if [ "$error_count" -ge "$ERROR_THRESHOLD" ]; then
                log "连续检测到 $ERROR_THRESHOLD 次业务接口异常，准备重启服务"
                restart_service
                reset_error_count
            fi
        else
            # 如果接口正常，重置错误计数
            reset_error_count
        fi
        
        # 等待下次检查
        log "等待 $CHECK_INTERVAL 秒后进行下次检查..."
        sleep "$CHECK_INTERVAL"
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
    log "FQNovel智能监控脚本启动"
    
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