# Blue-Green Deployment 설정 완료 ✅

## 생성된 파일 목록

```
COTATO-Recruit-BE/
├── .github/
│   └── workflows/
│       └── deploy-dev.yml                # GitHub Actions CI/CD 워크플로우
├── deployment/
│   ├── DEPLOYMENT_SETUP.md              # 이 문서 (전체 설정 가이드)
│   └── dev/
│       ├── docker-compose.dev.yml       # Blue-Green 컨테이너 정의
│       ├── nginx.conf                   # Nginx 설정 (HTTPS)
│       └── deploy.sh                    # Blue-Green 배포 스크립트
├── Dockerfile                           # 최적화된 Docker 이미지 빌드 파일
└── .gitignore                           # .env 파일 제외 추가
```

**참고**: SSL_SETUP.md와 README.md는 온라인 문서로 확인 가능하므로 로컬에 유지할 필요 없습니다.

## 다음 단계 (Setup Checklist)

### 1. GitHub Secrets 설정 ⚙️

리포지토리의 `Settings > Secrets and variables > Actions`에서 다음을 추가하세요:

#### Docker Hub 관련
- [ ] `DOCKER_USERNAME` - Docker Hub 사용자명 (Docker 이미지를 저장할 계정)
- [ ] `DOCKER_PASSWORD` - Docker Hub 비밀번호 또는 Access Token

---

#### EC2 서버 접속 정보
- [ ] `DEV_SERVER_HOST` - EC2 퍼블릭 IP 주소 또는 도메인 (예: `3.35.123.456` 또는 `cotato-recruit.o-r.kr`)
- [ ] `DEV_SERVER_USERNAME` - SSH 접속 사용자명 (Ubuntu: `ubuntu`, Amazon Linux: `ec2-user`)
- [ ] `DEV_SERVER_SSH_KEY` - EC2 SSH Private Key 전체 내용 (`.pem` 파일 내용)
- [ ] `DEV_SERVER_PORT` - SSH 포트 (선택사항, 기본값: `22`)

---

#### 데이터베이스 환경변수 (RDS)
- [ ] `DB_URL` - 데이터베이스 JDBC URL
  - 형식: `jdbc:mysql://<RDS-엔드포인트>:3306/<데이터베이스명>?serverTimezone=Asia/Seoul`
  - 예시: `jdbc:mysql://cotato-db.abc123.ap-northeast-2.rds.amazonaws.com:3306/cotato_recruit?serverTimezone=Asia/Seoul`

- [ ] `DB_USERNAME` - 데이터베이스 사용자명 (RDS 생성 시 설정한 마스터 사용자명)
  - 예시: `admin`

- [ ] `DB_PASSWORD` - 데이터베이스 비밀번호 (RDS 생성 시 설정한 비밀번호)

---

#### 애플리케이션 환경변수
- [ ] `JWT_SECRET` - JWT 토큰 서명에 사용할 비밀 키 (최소 32자 이상 권장)

---

> 💡 **추가 환경변수가 필요한 경우:**
> 1. `deployment/dev/docker-compose.dev.yml`의 `environment` 섹션에 추가
> 2. GitHub Secrets에 등록
> 3. `.github/workflows/deploy-dev.yml`의 환경변수 설정 부분에 추가
>
> **예시 (이메일 전송 설정 추가 시):**
> - GitHub Secret: `MAIL_PASSWORD`
> - docker-compose.dev.yml: `MAIL_PASSWORD: ${MAIL_PASSWORD}`
> - deploy-dev.yml: `export MAIL_PASSWORD=${{ secrets.MAIL_PASSWORD }}`

### 2. 서버 환경 설정 🖥️

#### 서버에 SSH 접속
```bash
ssh your-username@your-server-ip
```

#### Docker 설치 (아직 설치 안 된 경우)
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 로그아웃 후 재로그인 (또는 다음 명령어 실행)
newgrp docker
```

#### SSL 인증서 설정 (Let's Encrypt)

**도메인**: `cotato-recruit.o-r.kr`

##### 1. DNS 설정 확인
먼저 도메인이 EC2 서버 IP를 가리키는지 확인:
```bash
# 로컬에서 실행
nslookup cotato-recruit.o-r.kr
```

##### 2. Certbot 설치 및 인증서 발급
```bash
# 서버 SSH 접속 후

# Certbot 설치
sudo apt update
sudo apt install certbot -y

# certbot 디렉토리 생성
mkdir -p ~/certbot/www

# 인증서 발급 (Standalone 모드 - 초기 설정 시)
# Nginx가 실행 중이면 먼저 중지
docker compose -f ~/docker-compose.dev.yml stop nginx 2>/dev/null || true

sudo certbot certonly --standalone \
  -d cotato-recruit.o-r.kr \
  --non-interactive \
  --agree-tos \
  --email your-email@example.com

# 내 이메일로 변경 필요

# 인증서를 홈 디렉토리로 복사
mkdir -p ~/ssl
sudo cp /etc/letsencrypt/live/cotato-recruit.o-r.kr/fullchain.pem ~/ssl/fullchain.pem
sudo cp /etc/letsencrypt/live/cotato-recruit.o-r.kr/privkey.pem ~/ssl/privkey.pem
sudo chown $USER:$USER ~/ssl/*.pem
chmod 600 ~/ssl/*.pem
```

##### 3. 자동 갱신 설정 (필수)
Let's Encrypt 인증서는 90일마다 갱신해야 합니다:

```bash
# 갱신 스크립트 생성
cat > ~/renew-letsencrypt.sh << 'EOF'
#!/bin/bash
set -e
echo "Let's Encrypt 인증서 갱신: $(date)"
sudo certbot renew --quiet --webroot -w ~/certbot/www
if [ -f /etc/letsencrypt/live/cotato-recruit.o-r.kr/fullchain.pem ]; then
  sudo cp /etc/letsencrypt/live/cotato-recruit.o-r.kr/fullchain.pem ~/ssl/fullchain.pem
  sudo cp /etc/letsencrypt/live/cotato-recruit.o-r.kr/privkey.pem ~/ssl/privkey.pem
  sudo chown $USER:$USER ~/ssl/*.pem
  chmod 600 ~/ssl/*.pem
  docker ps --format '{{.Names}}' | grep -q "nginx" && docker exec nginx nginx -s reload
  echo "✅ 인증서 갱신 완료"
fi
EOF

chmod +x ~/renew-letsencrypt.sh

# Cron Job 설정
crontab -e
# 다음 라인 추가: 0 3 * * * $HOME/renew-letsencrypt.sh >> $HOME/letsencrypt-renew.log 2>&1
```

### 3. 초기 배포 준비 🚀

#### 방법 1: GitHub Actions를 통한 자동 배포 (권장)
1. 변경사항을 `develop` 브랜치에 푸시
   ```bash
   git add .
   git commit -m "chore: Blue-Green 배포 설정 추가"
   git push origin develop
   ```
2. GitHub Actions에서 자동으로 빌드 및 배포 진행
3. GitHub Actions 탭에서 진행 상황 확인

#### 방법 2: 수동 배포
1. GitHub 리포지토리의 `Actions` 탭으로 이동
2. `Deploy to Development Server` 선택
3. `Run workflow` 버튼 클릭
4. `develop` 브랜치 선택 후 실행

### 4. 배포 확인 ✅

#### 서버 상태 확인
```bash
# 서버에 SSH 접속 후
cd ~

# 실행 중인 컨테이너 확인
docker ps

# 로그 확인
docker compose -f ./docker-compose.dev.yml logs -f
```

#### 헬스체크 확인
```bash
# 서버 내부에서 확인
curl https://cotato-recruit.o-r.kr/actuator/health

# HTTP -> HTTPS 리다이렉트 확인
curl -I http://cotato-recruit.o-r.kr

# 외부에서 브라우저로 확인
# https://cotato-recruit.o-r.kr/actuator/health
```

## Blue-Green 배포 프로세스 흐름

```
┌─────────────────────────────────────────────────────┐
│  1. GitHub Actions: 코드 빌드 & Docker 이미지 생성  │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  2. Docker Hub에 이미지 푸시                        │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  3. 서버로 배포 스크립트 및 설정 파일 전송          │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  4. Blue/Green 중 유휴 컨테이너 확인                │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  5. 유휴 컨테이너에 최신 이미지 배포                │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  6. 헬스체크 (최대 150초)                           │
│     - 성공: 다음 단계로                             │
│     - 실패: 롤백 및 배포 중단                       │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  7. Nginx upstream 전환 (트래픽 스위칭)             │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  8. 이전 활성 컨테이너 종료                         │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│  9. 배포 완료! 🎉                                   │
└─────────────────────────────────────────────────────┘
```

## 주요 특징

### 무중단 배포
- Blue와 Green 두 개의 컨테이너를 번갈아 사용
- 새 버전 배포 시 유휴 컨테이너에 먼저 배포
- 헬스체크 통과 후 트래픽 전환
- 기존 컨테이너는 트래픽 전환 후 안전하게 종료

### 자동 롤백
- 헬스체크 실패 시 자동으로 새 컨테이너 제거
- Nginx 설정 검증 실패 시 이전 설정으로 자동 복구
- 배포 실패 시 서비스 중단 없음

### 환경변수 보안
- GitHub Secrets를 통한 안전한 환경변수 관리
- `.env` 파일은 Git에 커밋되지 않음
- 서버 배포 시 자동으로 환경변수 주입

### HTTPS 지원
- Let's Encrypt 무료 SSL 인증서 사용
- HTTP(80) → HTTPS(443) 자동 리다이렉트
- TLS 1.2 이상, 강력한 암호화 알고리즘 적용
- Certbot으로 자동 갱신 (90일마다)
- 브라우저 보안 경고 없음
