#!/bin/sh

# 检查是否存在app.jar文件，如果不存在则使用解压后的目录结构
if [ -f "/app/app.jar" ]; then
    echo "使用JAR文件启动..."
    java ${JAVA_OPTS:''} \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /app/app.jar
else
    echo "使用解压后的目录结构启动..."
    java ${JAVA_OPTS:''} \
    -Djava.security.egd=file:/dev/./urandom \
    -cp /app/resources/:/app/classes/:/app/libs/* "com.anjia.unidbgserver.UnidbgServerApplication"
fi
