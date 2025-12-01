# Blue-Green 무중단 배포 가이드

## 파일 구조

```
COTATO-Recruit-BE/
├── .github/workflows/
│   ├── auto-release-on-develop-merge.yml    # develop merge 시 자동 release 생성
│   ├── create-hotfix-branch.yml             # Hotfix 브랜치 생성
│   ├── deploy-qa.yml                        # QA 서버 배포
│   ├── deploy-production.yml                # Production 서버 배포
│   └── create-tag-and-release.yml           # Tag 및 Release 생성
├── deployment/
│   ├── qa/
│   │   ├── docker-compose.qa.yml            # QA Blue-Green 컨테이너
│   │   ├── nginx-blue.conf                  # QA Blue Nginx 설정
│   │   ├── nginx-green.conf                 # QA Green Nginx 설정
│   │   └── deploy.sh                        # QA Blue-Green 배포 스크립트
│   └── production/
│       ├── docker-compose.prod.yml          # Production Blue-Green 컨테이너
│       ├── nginx-blue.conf                  # Production Blue Nginx 설정
│       ├── nginx-green.conf                 # Production Green Nginx 설정
│       └── deploy.sh                        # Production Blue-Green 배포 스크립트
└── Dockerfile                               # Docker 이미지 빌드 파일
```

## Blue-Green 배포 방식

### 개념

두 개의 동일한 환경(Blue, Green)을 유지하며 무중단으로 배포합니다:

1. **현재 활성 환경 확인** (Blue 또는 Green)
2. **유휴 환경에 새 버전 배포**
3. **헬스 체크** (최대 150초)
4. **트래픽 전환** (Nginx 설정 변경)
5. **이전 환경 종료**

### 장점

- **무중단 배포**: 사용자가 서비스 중단을 경험하지 않음
- **빠른 롤백**: 문제 발생 시 Nginx 설정만 변경하여 즉시 이전 버전으로 복구
- **안전한 배포**: 새 버전이 정상 작동하는지 확인 후 트래픽 전환

## 새로운 서버에 배포 환경 구축

### 1. 서버 준비

```bash
# SSH 접속
ssh username@server-ip

# 시스템 업데이트
sudo apt update && sudo apt upgrade -y
```

### 2. Docker 설치

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
newgrp docker

# 설치 확인
docker --version
```

### 3. SSL 인증서 설정

```bash
# Certbot 설치
sudo apt install certbot -y

# 인증서 발급
sudo certbot certonly --standalone \
  -d your-domain.com \
  --non-interactive \
  --agree-tos \
  --email your-email@example.com

# 인증서 복사
mkdir -p ~/ssl ~/certbot/www
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ~/ssl/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ~/ssl/
sudo chown $USER:$USER ~/ssl/*.pem
chmod 600 ~/ssl/*.pem
```

#### 인증서 자동 갱신

```bash
# 갱신 스크립트 생성
cat > ~/renew-letsencrypt.sh << 'EOF'
#!/bin/bash
set -e
sudo certbot renew --quiet --webroot -w ~/certbot/www
if [ -f /etc/letsencrypt/live/your-domain.com/fullchain.pem ]; then
  sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ~/ssl/
  sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ~/ssl/
  sudo chown $USER:$USER ~/ssl/*.pem
  chmod 600 ~/ssl/*.pem
  docker exec nginx nginx -s reload 2>/dev/null || true
fi
EOF

chmod +x ~/renew-letsencrypt.sh

# Cron Job 등록 (매일 새벽 3시)
(crontab -l 2>/dev/null; echo "0 3 * * * $HOME/renew-letsencrypt.sh >> $HOME/letsencrypt-renew.log 2>&1") | crontab -
```

### 4. GitHub Secrets 설정

GitHub 리포지토리 `Settings > Secrets and variables > Actions`에서 등록:

#### QA 서버
- `QA_SERVER_HOST`: QA 서버 IP
- `QA_SERVER_USERNAME`: SSH 사용자명
- `QA_SERVER_SSH_KEY`: SSH Private Key
- `QA_SERVER_PORT`: SSH 포트 (기본 22)

#### Production 서버
- `PROD_SERVER_HOST`: Production 서버 IP
- `PROD_SERVER_USERNAME`: SSH 사용자명
- `PROD_SERVER_SSH_KEY`: SSH Private Key
- `PROD_SERVER_PORT`: SSH 포트 (기본 22)

#### Docker Hub
- `DOCKER_USERNAME`: Docker Hub 사용자명
- `DOCKER_PASSWORD`: Docker Hub 비밀번호

#### 데이터베이스 (환경별 분리)
- `DB_URL`, `PROD_DB_URL`: JDBC URL
- `DB_USERNAME`, `PROD_DB_USERNAME`: DB 사용자명
- `DB_PASSWORD`, `PROD_DB_PASSWORD`: DB 비밀번호

#### 애플리케이션
- `JWT_SECRET`, `PROD_JWT_SECRET`: JWT 서명 키

### 5. 배포 확인

```bash
# 컨테이너 상태 확인
docker ps

# 로그 확인
docker compose -f ~/docker-compose.{qa|prod}.yml logs -f

# 헬스 체크
curl https://your-domain.com/actuator/health
```

## 배포 프로세스

```
1. 코드 푸시
   ↓
2. GitHub Actions: 빌드 & 테스트
   ↓
3. Docker 이미지 생성 & 푸시
   ↓
4. 서버 접속 & 배포 파일 전송
   ↓
5. Blue-Green 배포 스크립트 실행
   ├─ 유휴 컨테이너에 새 버전 배포
   ├─ 헬스 체크 (최대 150초)
   ├─ 성공 → Nginx 트래픽 전환
   └─ 실패 → 자동 롤백
   ↓
6. 배포 완료
```

## 트러블슈팅

### 컨테이너 상태 확인
```bash
docker ps
docker logs {blue|green|nginx}
```

### 헬스 체크 실패 시
```bash
# 컨테이너 로그 확인
docker logs blue
docker logs green

# 수동 헬스 체크
curl http://localhost:8080/actuator/health
```

### SSL 인증서 문제
```bash
# 인증서 확인
ls -la ~/ssl/
sudo certbot certificates

# 수동 갱신
sudo certbot renew
```

### 배포 롤백
```bash
# GitHub Actions에서 이전 Tag로 수동 재배포
# 또는 Nginx 설정 수동 변경
docker exec nginx cp /etc/nginx/templates/nginx-{blue|green}.conf /etc/nginx/conf.d/default.conf
docker exec nginx nginx -s reload
```
