# Database 설정 가이드

## 환경별 Database 구성

| 환경 | Database | DDL Auto | Swagger |
|------|----------|----------|---------|
| Local | MySQL (로컬) | update | ✅ HTTP |
| QA | MySQL (RDS) | update | ✅ HTTPS |
| Production | MySQL (RDS) | validate | ❌ 비활성화 |

---

## Local 환경

### 데이터베이스 설정

**1. MySQL 설치 및 실행**
```bash
# macOS
brew install mysql
brew services start mysql

# 또는 Docker 사용
docker run -d \
  --name mysql-local \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=recruit \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

**2. 데이터베이스 생성**
```sql
CREATE DATABASE recruit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. 연결 정보**
- **URL**: `jdbc:mysql://localhost:3306/recruit`
- **Username**: 로컬 DB 접속 사용자명
- **Password**: 로컬 DB 접속 비밀번호
- **Timezone**: `Asia/Seoul`

### 실행 방법
```bash
./gradlew bootRun
```

---

## QA 환경

### 환경변수 설정
```bash
export DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/recruit_qa?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
export DB_USERNAME=<username>
export DB_PASSWORD=<password>
export SERVER_URL=https://qa.cotato.com
```

### 데이터베이스 생성
```sql
CREATE DATABASE recruit_qa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Production 환경

### 환경변수 설정
```bash
export DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/recruit_prod?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=true
export DB_USERNAME=<username>
export DB_PASSWORD=<password>
export SERVER_URL=https://api.cotato.com
```

### 최초 DB 설정

**1. 데이터베이스 생성**
```sql
CREATE DATABASE recruit_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**2. 초기 스키마 구축**

QA 환경의 스키마를 덤프하여 운영에 적용:
```bash
# QA 스키마 덤프
mysqldump -h <RDS_ENDPOINT> -u <user> -p --no-data recruit_qa > schema.sql

# Production에 적용
mysql -h <RDS_ENDPOINT> -u <user> -p recruit_prod < schema.sql
```

### 주의사항
- Production은 `ddl-auto: validate` 모드
- Entity와 DB 스키마가 불일치하면 애플리케이션 시작 실패
- 스키마 변경은 배포 전에 수동으로 적용 필요
