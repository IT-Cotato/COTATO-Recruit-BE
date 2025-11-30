#!/bin/bash
set -e

# 서비스 및 컨테이너 이름 설정
BLUE_SERVICE="recruit-blue"
GREEN_SERVICE="recruit-green"
BLUE_CONTAINER="cotato-recruit-blue"
GREEN_CONTAINER="cotato-recruit-green"
NGINX_CONTAINER="nginx"

# Docker Compose 파일 경로
COMPOSE_FILE="./docker-compose.dev.yml"

# 헬스 체크 설정
MAX_RETRY=30
RETRY_INTERVAL=5

echo "=========================================="
echo "🚀 Blue-Green 배포 시작"
echo "=========================================="

# 1. 현재 활성 컨테이너 확인
echo ""
echo "1️⃣ 현재 활성 컨테이너 확인 중..."
if docker ps --format '{{.Names}}' | grep -q "$BLUE_CONTAINER"; then
    ACTIVE_CONTAINER=$BLUE_CONTAINER
    IDLE_CONTAINER=$GREEN_CONTAINER
    ACTIVE_SERVICE=$BLUE_SERVICE
    IDLE_SERVICE=$GREEN_SERVICE
    ACTIVE_COLOR="BLUE"
    IDLE_COLOR="GREEN"
else
    ACTIVE_CONTAINER=$GREEN_CONTAINER
    IDLE_CONTAINER=$BLUE_CONTAINER
    ACTIVE_SERVICE=$GREEN_SERVICE
    IDLE_SERVICE=$BLUE_SERVICE
    ACTIVE_COLOR="GREEN"
    IDLE_COLOR="BLUE"
fi

echo "   현재 활성: $ACTIVE_COLOR ($ACTIVE_CONTAINER)"
echo "   배포 대상: $IDLE_COLOR ($IDLE_CONTAINER)"

# 2. 최신 이미지 Pull
echo ""
echo "2️⃣ 최신 Docker 이미지 Pull 중..."
docker compose -f $COMPOSE_FILE pull $IDLE_SERVICE

# 3. IDLE 컨테이너 기존 인스턴스 정리
echo ""
echo "3️⃣ 기존 $IDLE_COLOR 컨테이너 정리 중..."
docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE

# 4. IDLE 컨테이너 시작
echo ""
echo "4️⃣ $IDLE_COLOR 컨테이너 시작 중..."
docker compose -f $COMPOSE_FILE up -d $IDLE_SERVICE

# 5. 헬스 체크
echo ""
echo "5️⃣ $IDLE_COLOR 컨테이너 헬스 체크 중..."
echo "   최대 $(($MAX_RETRY * $RETRY_INTERVAL))초 대기..."

for i in $(seq 1 $MAX_RETRY); do
    echo -n "   시도 $i/$MAX_RETRY: "

    # Docker 네트워크를 통해 컨테이너 이름으로 접근
    HEALTH_CHECK_URL="http://$IDLE_CONTAINER:8080/actuator/health"

    if docker exec $IDLE_CONTAINER curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 성공"
        HEALTH_CHECK_PASSED=true
        break
    else
        echo "⏳ 대기 중..."
        HEALTH_CHECK_PASSED=false

        if [ $i -eq $MAX_RETRY ]; then
            echo ""
            echo "❌ 헬스 체크 실패! 배포를 중단합니다."
            echo ""
            echo "📋 $IDLE_COLOR 컨테이너 로그:"
            docker compose -f $COMPOSE_FILE logs --tail=50 $IDLE_SERVICE
            echo ""
            echo "🔄 롤백: $IDLE_COLOR 컨테이너 제거 중..."
            docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
            exit 1
        fi
        sleep $RETRY_INTERVAL
    fi
done

if [ "$HEALTH_CHECK_PASSED" = false ]; then
    exit 1
fi

# 6. Nginx 설정 전환 (upstream 전환)
echo ""
echo "6️⃣ Nginx 설정 전환 중..."

# Nginx 컨테이너 실행 확인 및 시작
if ! docker ps --format '{{.Names}}' | grep -q "^$NGINX_CONTAINER$"; then
    echo "   ⚠️  Nginx 컨테이너가 실행 중이 아닙니다. 시작합니다..."
    docker compose -f $COMPOSE_FILE up -d $NGINX_CONTAINER
    sleep 5
    echo "   ✅ Nginx 컨테이너 시작 완료"
fi

if [ "$IDLE_COLOR" = "BLUE" ]; then
    NEW_CONFIG="nginx-blue.conf"
    ACTIVE_CONFIG="Blue"
else
    NEW_CONFIG="nginx-green.conf"
    ACTIVE_CONFIG="Green"
fi

echo "   새로운 설정: $NEW_CONFIG"

# 컨테이너 내부에서 설정 파일 복사
docker exec $NGINX_CONTAINER cp /etc/nginx/conf.d/$NEW_CONFIG /etc/nginx/conf.d/default.conf

# Nginx 설정 테스트
echo "   Nginx 설정 테스트 중..."
if docker exec $NGINX_CONTAINER nginx -t 2>&1 | grep -q "successful"; then
    echo "   ✅ Nginx 설정 검증 성공"
else
    echo "   ❌ Nginx 설정 검증 실패! 롤백합니다."
    docker compose -f $COMPOSE_FILE rm -f -s $IDLE_SERVICE
    exit 1
fi

# Nginx 리로드
echo "   Nginx 리로드 중..."
docker exec $NGINX_CONTAINER nginx -s reload
echo "   ✅ Nginx 리로드 완료 ($ACTIVE_CONFIG 환경으로 전환)"

# 7. 이전 컨테이너 종료
echo ""
echo "7️⃣ 이전 $ACTIVE_COLOR 컨테이너 종료 중..."
sleep 3  # Nginx 리로드 후 안정화 대기
docker compose -f $COMPOSE_FILE stop $ACTIVE_SERVICE
echo "   ✅ $ACTIVE_COLOR 컨테이너 종료 완료"

# 8. 배포 완료
echo ""
echo "=========================================="
echo "✅ 배포 완료!"
echo "=========================================="
echo "   새로운 활성 컨테이너: $IDLE_COLOR ($IDLE_CONTAINER)"
echo "   Nginx 설정 파일: $NEW_CONFIG"
echo "=========================================="