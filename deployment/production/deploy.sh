#!/bin/bash
set -e

# 서비스 및 컨테이너 이름 설정
BLUE_SERVICE="blue"
GREEN_SERVICE="green"
BLUE_CONTAINER="blue"
GREEN_CONTAINER="green"
NGINX_CONTAINER="nginx"

# Docker Compose 파일 경로
COMPOSE_FILE="./docker-compose.prod.yml"

# 헬스 체크 설정
MAX_RETRY=30
RETRY_INTERVAL=5

echo "=========================================="
echo "Blue-Green Deployment Started"
echo "=========================================="

# 1. Redis 컨테이너 확인 및 시작
if ! docker ps --format '{{.Names}}' | grep -q "^redis$"; then
    echo "[1/8] Starting Redis container..."
    docker compose -f $COMPOSE_FILE up -d redis
    sleep 5
else
    echo "[1/8] Redis container already running"
fi

# 2. 현재 활성 컨테이너 확인
BLUE_RUNNING=$(docker ps --format '{{.Names}}' | grep -q "^$BLUE_CONTAINER$" && echo "true" || echo "false")
GREEN_RUNNING=$(docker ps --format '{{.Names}}' | grep -q "^$GREEN_CONTAINER$" && echo "true" || echo "false")

if [ "$BLUE_RUNNING" = "true" ]; then
    ACTIVE_CONTAINER=$BLUE_CONTAINER
    IDLE_CONTAINER=$GREEN_CONTAINER
    ACTIVE_SERVICE=$BLUE_SERVICE
    IDLE_SERVICE=$GREEN_SERVICE
    ACTIVE_COLOR="Blue"
    IDLE_COLOR="Green"
elif [ "$GREEN_RUNNING" = "true" ]; then
    ACTIVE_CONTAINER=$GREEN_CONTAINER
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=$GREEN_SERVICE
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="Green"
    IDLE_COLOR="Blue"
else
    # 첫 배포: 둘 다 없는 경우 Blue부터 시작
    echo "[2/8] Initial deployment detected"
    ACTIVE_CONTAINER=""
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=""
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="None"
    IDLE_COLOR="Blue"
fi

echo "[2/8] Active: $ACTIVE_COLOR -> Target: $IDLE_COLOR"

# 3. 최신 이미지 Pull
echo "[3/8] Pulling latest image for $IDLE_SERVICE..."
docker compose -f $COMPOSE_FILE pull $IDLE_SERVICE

# 4. IDLE 컨테이너 기존 인스턴스 정리
docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE 2>/dev/null || true

# 5. IDLE 컨테이너 시작
echo "[4/8] Starting $IDLE_COLOR container..."
docker compose -f $COMPOSE_FILE up -d $IDLE_SERVICE

# 6. 헬스 체크
echo "[5/8] Health checking (max $(($MAX_RETRY * $RETRY_INTERVAL))s)..."

HEALTH_CHECK_PASSED=false

for i in $(seq 1 $MAX_RETRY); do
    if docker exec $IDLE_CONTAINER curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "[5/8] Health check passed ($i/$MAX_RETRY)"
        HEALTH_CHECK_PASSED=true
        break
    fi

    echo "[5/8] Waiting... ($i/$MAX_RETRY)"
    sleep $RETRY_INTERVAL
done

if [ "$HEALTH_CHECK_PASSED" = false ]; then
    echo "[ERROR] Health check failed. Deployment aborted."
    echo "Container logs:"
    docker compose -f $COMPOSE_FILE logs --tail=50 $IDLE_SERVICE
    docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
    exit 1
fi

# 6.5. Nginx 컨테이너 확인 및 시작 (IDLE 컨테이너가 준비된 후)
if ! docker ps --format '{{.Names}}' | grep -q "^$NGINX_CONTAINER$"; then
    echo "[5.5/8] Starting Nginx container..."
    docker compose -f $COMPOSE_FILE up -d $NGINX_CONTAINER
    sleep 5

    # 첫 배포 시 default.conf 초기화
    if ! docker exec $NGINX_CONTAINER test -f /etc/nginx/conf.d/default.conf 2>/dev/null; then
        echo "[5.5/8] Initializing Nginx default.conf..."
        IDLE_COLOR_LOWER=$(echo "$IDLE_COLOR" | tr '[:upper:]' '[:lower:]')
        docker exec $NGINX_CONTAINER cp /etc/nginx/templates/nginx-$IDLE_COLOR_LOWER.conf /etc/nginx/conf.d/default.conf
        docker exec $NGINX_CONTAINER nginx -s reload
    fi
else
    echo "[5.5/8] Nginx container already running"
fi

# 7. Nginx 설정 전환
echo "[6/8] Switching traffic to $IDLE_COLOR..."

if [ "$IDLE_COLOR" = "Blue" ]; then
    NEW_CONFIG="nginx-blue.conf"
else
    NEW_CONFIG="nginx-green.conf"
fi

# 현재 설정 백업
docker exec $NGINX_CONTAINER cp /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.backup 2>/dev/null || true

# 새 설정 적용
docker exec $NGINX_CONTAINER cp /etc/nginx/templates/$NEW_CONFIG /etc/nginx/conf.d/default.conf

# 설정 검증
if ! docker exec $NGINX_CONTAINER nginx -t 2>&1 | grep -q "successful"; then
    echo "[ERROR] Nginx config validation failed. Rolling back..."
    docker exec $NGINX_CONTAINER cp /etc/nginx/conf.d/default.conf.backup /etc/nginx/conf.d/default.conf 2>/dev/null || true
    docker exec $NGINX_CONTAINER nginx -s reload 2>/dev/null || true
    docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
    exit 1
fi

docker exec $NGINX_CONTAINER nginx -s reload
echo "[6/8] Traffic switched: $ACTIVE_COLOR -> $IDLE_COLOR"

# 8. 이전 컨테이너 종료 및 삭제
if [ -n "$ACTIVE_SERVICE" ]; then
    echo "[7/8] Waiting for graceful shutdown (30s)..."
    sleep 30
    echo "[7/8] Removing old container..."
    docker compose -f $COMPOSE_FILE rm -f -s $ACTIVE_SERVICE
else
    echo "[7/8] No previous container to remove (initial deployment)"
fi

# 9. 배포 완료
echo "[8/8] Deployment completed. Active: $IDLE_COLOR"
echo "=========================================="
