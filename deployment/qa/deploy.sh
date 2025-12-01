#!/bin/bash
set -e

# 서비스 및 컨테이너 이름 설정
BLUE_SERVICE="blue"
GREEN_SERVICE="green"
BLUE_CONTAINER="blue"
GREEN_CONTAINER="green"
NGINX_CONTAINER="nginx"

# Docker Compose 파일 경로
COMPOSE_FILE="./docker-compose.qa.yml"

# 헬스 체크 설정
MAX_RETRY=30
RETRY_INTERVAL=5

echo "=========================================="
echo "🚀 Blue-Green 배포 시작"
echo "=========================================="

# 1. 현재 활성 컨테이너 확인
if docker ps --format '{{.Names}}' | grep -q "^$BLUE_CONTAINER$"; then
    ACTIVE_CONTAINER=$BLUE_CONTAINER
    IDLE_CONTAINER=$GREEN_CONTAINER
    ACTIVE_SERVICE=$BLUE_SERVICE
    IDLE_SERVICE=$GREEN_SERVICE
    ACTIVE_COLOR="Blue"
    IDLE_COLOR="Green"
else
    ACTIVE_CONTAINER=$GREEN_CONTAINER
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=$GREEN_SERVICE
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="Green"
    IDLE_COLOR="Blue"
fi

echo "현재 활성: $ACTIVE_COLOR → 배포 대상: $IDLE_COLOR"
echo ""

# 2. 최신 이미지 Pull
echo "📥 최신 이미지 다운로드 중..."
docker compose -f $COMPOSE_FILE pull $IDLE_SERVICE

# 3. IDLE 컨테이너 기존 인스턴스 정리
docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE 2>/dev/null || true

# 4. IDLE 컨테이너 시작
echo "🔄 $IDLE_COLOR 컨테이너 시작 중..."
docker compose -f $COMPOSE_FILE up -d $IDLE_SERVICE

# 5. 헬스 체크
echo "⏳ 헬스 체크 중 (최대 $(($MAX_RETRY * $RETRY_INTERVAL))초)..."

for i in $(seq 1 $MAX_RETRY); do
    if docker exec $IDLE_CONTAINER curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 헬스 체크 성공 ($i/$MAX_RETRY)"
        HEALTH_CHECK_PASSED=true
        break
    fi

    if [ $i -eq $MAX_RETRY ]; then
        echo "❌ 헬스 체크 실패! 배포 중단"
        echo ""
        echo "📋 컨테이너 로그:"
        docker compose -f $COMPOSE_FILE logs --tail=50 $IDLE_SERVICE
        docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
        exit 1
    fi

    sleep $RETRY_INTERVAL
done

if [ "$HEALTH_CHECK_PASSED" = false ]; then
    exit 1
fi

# 6. Nginx 설정 전환
echo "🔀 Nginx 트래픽 전환 중..."

if [ "$IDLE_COLOR" = "Blue" ]; then
    NEW_CONFIG="nginx-blue.conf"
else
    NEW_CONFIG="nginx-green.conf"
fi

docker exec $NGINX_CONTAINER cp /etc/nginx/templates/$NEW_CONFIG /etc/nginx/conf.d/default.conf

if ! docker exec $NGINX_CONTAINER nginx -t 2>&1 | grep -q "successful"; then
    echo "❌ Nginx 설정 검증 실패! 롤백"
    docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
    exit 1
fi

docker exec $NGINX_CONTAINER nginx -s reload
echo "✅ 트래픽 전환 완료: $ACTIVE_COLOR → $IDLE_COLOR"

# 7. 이전 컨테이너 종료
sleep 3
docker compose -f $COMPOSE_FILE stop $ACTIVE_SERVICE

# 8. 배포 완료
echo ""
echo "=========================================="
echo "✅ 배포 완료! 활성 컨테이너: $IDLE_COLOR"
echo "=========================================="
