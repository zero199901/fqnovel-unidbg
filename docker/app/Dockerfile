# 显式指定 amd64 架构
FROM --platform=linux/amd64 eclipse-temurin:17-jre


WORKDIR /app

# Default application port inside containers
ENV SERVER_PORT=8757

# Copy entrypoint
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 8757

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
