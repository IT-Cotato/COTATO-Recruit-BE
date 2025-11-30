# Blue-Green 무중단 배포 가이드

이 문서는 COTATO Recruit 백엔드 프로젝트의 Blue-Green 무중단 배포 시스템 구축 과정과 새로운 서버에 동일한 환경을 구축하는 방법을 설명합니다.

## 📁 파일 구조

```
COTATO-Recruit-BE/
├── .github/
│   └── workflows/
│       └── deploy-qa.yml                # GitHub Actions CI/CD 워크플로우
├── deployment/
│   └── qa/
│       ├── docker-compose.qa.yml        # Blue-Green 컨테이너 정의
│       ├── nginx-blue.conf              # Blue 환경 Nginx 설정
│       ├── nginx-green.conf             # Green 환경 Nginx 설정
│       └── deploy.sh                    # Blue-Green 배포 스크립트
├── docs/
│   ├── deployment-setup.md              # 이 문서
│   └── branch-strategy.md               # 브랜치 전략 문서
└── Dockerfile                           # Docker 이미지 빌드 파일
```

---

## 🏗️ 배포 시스템 구축 과정

### 1️⃣ Docker 환경 구성

#### Dockerfile 작성
멀티 스테이지 빌드를 사용하여 이미지 크기를 최적화했습니다.

```dockerfile
# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.qa.yml 작성
Blue, Green 두 개의 애플리케이션 컨테이너와 Nginx 리버스 프록시를 정의했습니다.

```yaml
services:
  blue:
    container_name: blue
    image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
    environment:
      SPRING_PROFILES_ACTIVE: qa
      DB_URL: ${DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    networks:
      - app_network

  green:
    container_name: green
    image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
    environment:
      SPRING_PROFILES_ACTIVE: qa
      DB_URL: ${DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    networks:
      - app_network

  nginx:
    image: nginx:latest
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx-blue.conf:/etc/nginx/templates/nginx-blue.conf:ro
      - ./nginx-green.conf:/etc/nginx/templates/nginx-green.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
      - ./certbot/www:/var/www/certbot:ro
    networks:
      - app_network
```

### 2️⃣ Nginx 설정

Blue와 Green 환경에 대한 별도의 Nginx 설정 파일을 작성했습니다.

**nginx-blue.conf**
```nginx
upstream recruit {
    server blue:8080;
}

server {
    listen 80;
    server_name cotato-recruit.o-r.kr;

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl;
    http2 on;
    server_name cotato-recruit.o-r.kr;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;

    location / {
        proxy_pass http://recruit;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**nginx-green.conf**는 `upstream`의 `server blue:8080`을 `server green:8080`으로 변경한 버전입니다.

### 3️⃣ Blue-Green 배포 스크립트

`deploy.sh` 스크립트는 다음 과정을 자동화합니다:

1. **현재 활성 컨테이너 확인** - Blue 또는 Green 중 어떤 것이 실행 중인지 확인
2. **최신 이미지 다운로드** - Docker Hub에서 최신 이미지 Pull
3. **유휴 컨테이너 시작** - 사용하지 않는 컨테이너에 새 버전 배포
4. **헬스 체크** - 새 컨테이너가 정상 작동하는지 확인 (최대 150초)
5. **트래픽 전환** - Nginx 설정을 변경하여 새 컨테이너로 트래픽 전환
6. **이전 컨테이너 종료** - 기존 컨테이너 중지

실패 시 자동으로 롤백하여 서비스 중단을 방지합니다.

### 4️⃣ GitHub Actions CI/CD

`.github/workflows/deploy-qa.yml`에서 다음을 자동화했습니다:

1. **빌드 및 테스트** - Gradle로 애플리케이션 빌드 및 테스트
2. **Docker 이미지 생성** - 멀티 스테이지 빌드로 이미지 생성
3. **Docker Hub 푸시** - 이미지를 레지스트리에 업로드
4. **서버 배포** - SSH로 서버 접속 후 배포 스크립트 실행

---

## 🚀 새로운 서버에 배포 환경 구축하기

다른 서버에 동일한 배포 시스템을 구축하려면 다음 단계를 따르세요.

### Step 1: 서버 준비

#### 1-1. SSH 접속
```bash
ssh username@server-ip
```

#### 1-2. 시스템 업데이트
```bash
sudo apt update
sudo apt upgrade -y
```

### Step 2: Docker 설치

```bash
# Docker 설치 스크립트 다운로드 및 실행
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 그룹 변경 적용 (재로그인 대신)
newgrp docker

# Docker 설치 확인
docker --version
docker ps
```

### Step 3: SSL 인증서 설정

#### 3-1. DNS 설정 확인
도메인이 서버 IP를 가리키는지 확인합니다.

```bash
# 로컬 PC에서 실행
nslookup your-domain.com
```

#### 3-2. Certbot 설치
```bash
sudo apt update
sudo apt install certbot -y
```

#### 3-3. SSL 인증서 발급
```bash
# certbot 디렉토리 생성
mkdir -p ~/certbot/www

# 인증서 발급 (Standalone 모드)
sudo certbot certonly --standalone \
  -d your-domain.com \
  --non-interactive \
  --agree-tos \
  --email your-email@example.com

# 인증서를 홈 디렉토리로 복사
mkdir -p ~/ssl
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ~/ssl/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ~/ssl/
sudo chown $USER:$USER ~/ssl/*.pem
chmod 600 ~/ssl/*.pem

# 인증서 확인
ls -l ~/ssl/
```

#### 3-4. 인증서 자동 갱신 설정
Let's Encrypt 인증서는 90일마다 갱신해야 합니다.

```bash
# 갱신 스크립트 생성
cat > ~/renew-letsencrypt.sh << 'EOF'
#!/bin/bash
set -e
echo "Let's Encrypt 인증서 갱신: $(date)"
sudo certbot renew --quiet --webroot -w ~/certbot/www
if [ -f /etc/letsencrypt/live/your-domain.com/fullchain.pem ]; then
  sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ~/ssl/
  sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ~/ssl/
  sudo chown $USER:$USER ~/ssl/*.pem
  chmod 600 ~/ssl/*.pem
  docker ps --format '{{.Names}}' | grep -q "nginx" && docker exec nginx nginx -s reload
  echo "✅ 인증서 갱신 완료"
fi
EOF

chmod +x ~/renew-letsencrypt.sh

# Cron Job 등록
(crontab -l 2>/dev/null; echo "0 3 * * * $HOME/renew-letsencrypt.sh >> $HOME/letsencrypt-renew.log 2>&1") | crontab -

# Cron Job 확인
crontab -l
```

### Step 4: GitHub Secrets 설정

GitHub 리포지토리의 `Settings > Secrets and variables > Actions`에서 다음을 등록하세요:

#### Docker Hub
- `DOCKER_USERNAME` - Docker Hub 사용자명
- `DOCKER_PASSWORD` - Docker Hub 비밀번호 또는 Access Token

#### 서버 접속 정보
- `QA_SERVER_HOST` - 서버 IP 또는 도메인 (예: `3.35.123.456`)
- `QA_SERVER_USERNAME` - SSH 사용자명 (Ubuntu: `ubuntu`)
- `QA_SERVER_SSH_KEY` - SSH Private Key 전체 내용 (`.pem` 파일)
- `QA_SERVER_PORT` - SSH 포트 (기본값: `22`)

#### 데이터베이스
- `DB_URL` - JDBC URL (예: `jdbc:mysql://db-host:3306/database?serverTimezone=Asia/Seoul`)
- `DB_USERNAME` - DB 사용자명
- `DB_PASSWORD` - DB 비밀번호

#### 애플리케이션
- `JWT_SECRET` - JWT 서명 키 (최소 32자 이상)

### Step 5: 배포 실행

#### 방법 1: GitHub Actions 자동 배포 (권장)
```bash
git add .
git commit -m "chore: 배포 설정 추가"
git push origin release
```

GitHub Actions가 자동으로 빌드 및 배포를 진행합니다.

#### 방법 2: 수동 배포
1. GitHub 리포지토리의 `Actions` 탭 이동
2. `Deploy to QA Server` 워크플로우 선택
3. `Run workflow` 클릭
4. 브랜치 선택 후 실행

### Step 6: 배포 확인

#### 컨테이너 상태 확인
```bash
# 서버 SSH 접속
ssh username@server-ip

# 실행 중인 컨테이너 확인
docker ps

# 로그 확인
docker compose -f ~/docker-compose.qa.yml logs -f
```

#### 헬스 체크
```bash
# 서버 내부에서
curl https://your-domain.com/actuator/health

# 외부에서 (브라우저 또는 로컬)
curl https://your-domain.com/actuator/health
```

#### 리소스 모니터링
```bash
# 메모리 사용량
free -h

# 디스크 사용량
df -h

# Docker 컨테이너 리소스
docker stats --no-stream
```

---

## 📊 Blue-Green 배포 프로세스

```
1. GitHub에 코드 푸시
   ↓
2. GitHub Actions: 빌드 & 테스트
   ↓
3. Docker 이미지 생성 & Docker Hub 푸시
   ↓
4. 서버 접속 & 배포 파일 전송
   ↓
5. 현재 활성 컨테이너 확인 (Blue/Green)
   ↓
6. 유휴 컨테이너에 최신 이미지 배포
   ↓
7. 헬스 체크 (최대 150초)
   ├─ 성공 → 다음 단계
   └─ 실패 → 롤백 & 종료
   ↓
8. Nginx 설정 변경 (트래픽 전환)
   ├─ Blue → Green 또는
   └─ Green → Blue
   ↓
9. 이전 활성 컨테이너 종료
   ↓
10. 배포 완료 ✅
```

---

## 🔧 트러블슈팅

### Nginx 컨테이너가 시작되지 않는 경우
```bash
# Nginx 로그 확인
docker logs nginx

# 설정 파일 확인
docker exec nginx ls -la /etc/nginx/templates/
docker exec nginx ls -la /etc/nginx/conf.d/

# Nginx 재시작
docker compose -f ~/docker-compose.qa.yml restart nginx
```

### 헬스 체크 실패 시
```bash
# 컨테이너 로그 확인
docker logs blue
docker logs green

# 컨테이너 내부에서 헬스 체크
docker exec blue curl -s http://localhost:8080/actuator/health
docker exec green curl -s http://localhost:8080/actuator/health
```

### SSL 인증서 문제
```bash
# 인증서 파일 확인
ls -la ~/ssl/

# 인증서 유효기간 확인
sudo certbot certificates

# 수동 갱신
sudo certbot renew
```
