#!/bin/bash
set -euo pipefail

JAR_PATH=${JAR_PATH:-/app/app.jar}
JAVA_OPTS=${JAVA_OPTS:-""}
SERVER_PORT=${SERVER_PORT:-8080}

exec java -Dserver.port=${SERVER_PORT} ${JAVA_OPTS} -jar ${JAR_PATH}

