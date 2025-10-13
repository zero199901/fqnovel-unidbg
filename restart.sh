#!/bin/bash
set -e  # 遇到错误立即退出
echo "[$(date)] 正在重启项目..."

# 第一步：请求注册接口（无参，生成随机真实设备并写入配置）
echo "[$(date)] 调用注册接口，写入新设备配置..."
curl -sf -X POST 'http://127.0.0.1:9999/api/device/register-and-restart' \
  -H 'Content-Type: application/json' \
  -d '{}' > /dev/null
echo "[$(date)] 注册接口调用成功，准备重启进程..."

# 停止现有的Java进程
echo "[$(date)] 停止现有进程..."
pkill -f "unidbg-boot-server" || echo "没有找到现有进程"
sleep 3

# 确保端口9999被释放
echo "[$(date)] 检查端口9999(仅结束Java监听者)..."
# 只结束占用9999端口的Java进程，避免误杀 Reqable 等其他软件
JAVA_PIDS=$(lsof -nP -iTCP:9999 -sTCP:LISTEN 2>/dev/null | awk '/java/ {print $2}' | sort -u)
if [ -n "$JAVA_PIDS" ]; then
  echo "[$(date)] 结束占用9999端口的Java进程: $JAVA_PIDS"
  echo "$JAVA_PIDS" | xargs kill -9 2>/dev/null || true
else
  echo "[$(date)] 端口9999无Java监听者"
fi
sleep 2

# 检查JAR文件是否存在
if [ ! -f "target/unidbg-boot-server-0.0.1-SNAPSHOT.jar" ]; then
    echo "[$(date)] 错误: JAR文件不存在: target/unidbg-boot-server-0.0.1-SNAPSHOT.jar"
    exit 1
fi

# 启动新的JAR文件
echo "[$(date)] 启动JAR文件: target/unidbg-boot-server-0.0.1-SNAPSHOT.jar"
cd "/Users/edy/code/cursor/nixiang/douyinsix/fqnovel-unidbg"
nohup java -jar "target/unidbg-boot-server-0.0.1-SNAPSHOT.jar" > target/spring-boot.log 2>&1 &
JAVA_PID=$!
echo "[$(date)] 新进程PID: $JAVA_PID"

# 等待进程启动
echo "[$(date)] 等待进程启动..."
sleep 5

# 检查进程是否还在运行
if ps -p $JAVA_PID > /dev/null; then
    echo "[$(date)] 项目重启成功，PID: $JAVA_PID"
else
    echo "[$(date)] 错误: 进程启动失败"
    echo "[$(date)] 检查日志: target/spring-boot.log"
    tail -20 target/spring-boot.log
    exit 1
fi
