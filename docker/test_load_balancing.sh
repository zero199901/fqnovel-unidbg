#!/bin/bash

# 负载均衡测试脚本
# 用于验证nginx是否将请求均匀分布到各个应用节点

echo "=== 负载均衡测试脚本 ==="
echo "测试nginx是否将请求均匀分布到5个应用节点"
echo ""

# 检查nginx是否运行
if ! docker ps | grep -q "fqnovel-nginx"; then
    echo "错误: nginx容器未运行，请先启动服务"
    echo "运行命令: docker-compose up -d"
    exit 1
fi

# 检查应用容器是否运行
app_count=$(docker ps | grep -c "fqnovel-app-")
if [ "$app_count" -lt 5 ]; then
    echo "警告: 只有 $app_count 个应用容器在运行，期望5个"
fi

echo "开始发送测试请求..."
echo ""

# 发送多个请求并记录响应
request_count=20
echo "发送 $request_count 个请求到nginx负载均衡器..."

# 创建临时文件记录结果
temp_file=$(mktemp)

for i in $(seq 1 $request_count); do
    # 发送请求到nginx
    response=$(curl -s -w "%{http_code}" -o /dev/null "http://localhost:8756/nginx-health" 2>/dev/null)
    
    if [ "$response" = "200" ]; then
        echo "请求 $i: 成功 (HTTP $response)"
    else
        echo "请求 $i: 失败 (HTTP $response)"
    fi
    
    # 短暂延迟避免过快请求
    sleep 0.1
done

echo ""
echo "=== 检查nginx访问日志 ==="
echo "查看最近的访问日志，验证请求分布..."

# 获取nginx容器的访问日志
echo "nginx访问日志 (最近20条):"
docker logs fqnovel-nginx --tail 20 2>/dev/null | grep -E "(GET|POST)" || echo "未找到访问日志"

echo ""
echo "=== 检查应用容器日志 ==="
echo "检查各个应用容器的健康检查日志..."

for i in {1..5}; do
    container_name="fqnovel-app-$i"
    if docker ps | grep -q "$container_name"; then
        echo "--- $container_name ---"
        docker logs "$container_name" --tail 5 2>/dev/null | grep -E "(health|started|running)" || echo "无相关日志"
    else
        echo "--- $container_name: 未运行 ---"
    fi
done

echo ""
echo "=== 负载均衡算法说明 ==="
echo "当前配置使用轮询算法 (round-robin)"
echo "- 请求会按顺序分发到 app1 -> app2 -> app3 -> app4 -> app5 -> app1..."
echo "- 每个服务器接收相等数量的请求"
echo "- 如果某个服务器不可用，nginx会自动跳过它"
echo ""
echo "其他可选的负载均衡算法:"
echo "- least_conn: 最少连接数算法"
echo "- ip_hash: 基于客户端IP的哈希算法 (已移除)"
echo "- hash: 基于自定义键的哈希算法"

# 清理临时文件
rm -f "$temp_file"

echo ""
echo "测试完成！"
echo "如果看到请求分布不均匀，请检查:"
echo "1. 所有应用容器是否正常运行"
echo "2. nginx配置是否正确重新加载"
echo "3. 网络连接是否正常"
