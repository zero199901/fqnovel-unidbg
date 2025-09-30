#!/bin/bash

# 启动脚本，过滤METASEC错误信息
echo "启动FQNovel Unidbg服务..."
echo "服务地址: http://localhost:9091"
echo "设备轮换管理: http://localhost:9091/api/device-rotation/info"
echo ""

# 使用grep过滤掉METASEC相关的错误信息
mvn spring-boot:run 2>&1 | grep -v "METASEC" | grep -v "MSTaskManager"
