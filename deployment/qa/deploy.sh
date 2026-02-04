#!/bin/bash
set -e

# 서비스 및 컨테이너 이름 설정
BLUE_SERVICE="qa-blue"
GREEN_SERVICE="qa-green"
BLUE_CONTAINER="qa-blue"
GREEN_CONTAINER="qa-green"
REDIS_SERVICE="qa-redis"
NGINX_CONTAINER="nginx"
ENV_PREFIX="qa"

# Docker Compose 파일 경로
COMPOSE_FILE="./docker-compose.qa.yml"
NGINX_COMPOSE_FILE="../nginx/docker-compose.nginx.yml"
NGINX_DIR="../nginx"

# 헬스 체크 설정
MAX_RETRY=30
RETRY_INTERVAL=5

echo "=========================================="
echo "QA Blue-Green Deployment Started"
echo "=========================================="

# SSL 인증서 확인
if [ ! -f ~/ssl/qa/fullchain.pem ] || [ ! -f ~/ssl/qa/privkey.pem ]; then
    echo "[ERROR] SSL certificates not found at ~/ssl/qa/"
    echo "[ERROR] Please issue certificates first"
    exit 1
fi
echo "[INFO] SSL certificates found"

# certbot 디렉토리 생성
mkdir -p ~/certbot/www

# 네트워크 생성 (없으면 생성)
docker network create qa_network 2>/dev/null || true
docker network create prod_network 2>/dev/null || true

# nginx conf.d 디렉토리 생성
mkdir -p $NGINX_DIR/conf.d

# 1. Redis 컨테이너 확인 및 시작
if ! docker ps --format '{{.Names}}' | grep -q "^$REDIS_SERVICE$"; then
    echo "[1/8] Starting Redis container..."
    docker compose -f $COMPOSE_FILE up -d $REDIS_SERVICE
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
    ACTIVE_COLOR="blue"
    IDLE_COLOR="green"
elif [ "$GREEN_RUNNING" = "true" ]; then
    ACTIVE_CONTAINER=$GREEN_CONTAINER
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=$GREEN_SERVICE
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="green"
    IDLE_COLOR="blue"
else
    echo "[2/8] Initial deployment detected"
    ACTIVE_CONTAINER=""
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=""
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="none"
    IDLE_COLOR="blue"
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

# 6.5. Nginx 컨테이너 확인 및 시작
if ! docker ps --format '{{.Names}}' | grep -q "^$NGINX_CONTAINER$"; then
    echo "[5.5/8] Starting Nginx container..."

    # 중지된 nginx 컨테이너가 있으면 삭제
    docker rm -f $NGINX_CONTAINER 2>/dev/null || true

    # 초기 설정 파일 복사
    cp $NGINX_DIR/templates/${ENV_PREFIX}-${IDLE_COLOR}.conf $NGINX_DIR/conf.d/${ENV_PREFIX}.conf

    docker compose -f $NGINX_COMPOSE_FILE up -d
    sleep 5
else
    echo "[5.5/8] Nginx container already running"
fi

# 7. Nginx 설정 전환
echo "[6/8] Switching traffic to $IDLE_COLOR..."

NEW_CONFIG="${ENV_PREFIX}-${IDLE_COLOR}.conf"

# 현재 설정 백업
cp $NGINX_DIR/conf.d/${ENV_PREFIX}.conf $NGINX_DIR/conf.d/${ENV_PREFIX}.conf.backup 2>/dev/null || true

# 새 설정 적용
cp $NGINX_DIR/templates/$NEW_CONFIG $NGINX_DIR/conf.d/${ENV_PREFIX}.conf

# 설정 검증 및 reload
if ! docker exec $NGINX_CONTAINER nginx -t 2>&1 | grep -q "successful"; then
    echo "[ERROR] Nginx config validation failed. Rolling back..."
    cp $NGINX_DIR/conf.d/${ENV_PREFIX}.conf.backup $NGINX_DIR/conf.d/${ENV_PREFIX}.conf 2>/dev/null || true
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
echo "[8/8] QA Deployment completed. Active: $IDLE_COLOR"
echo "=========================================="
