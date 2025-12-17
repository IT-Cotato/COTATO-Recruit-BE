# Google OAuth2 로그인 가이드

## 인증 플로우

```
1. 프론트엔드 → Google로 Authorization Code 요청
2. Google → 프론트엔드로 Code 반환 (Redirect)
3. 프론트엔드 → 백엔드로 Code 전송
4. 백엔드 → Google에서 Access Token + 사용자 정보 조회
5. 백엔드 → 회원 확인 (신규 회원가입 or 로그인)
6. 백엔드 → JWT 토큰 발급
```

---

## API 엔드포인트

### 1. 구글 로그인

```http
POST /api/auth/login/google
Content-Type: application/json

{
  "code": "4/0AfJohXk...",
  "redirectUri": "http://localhost:3000/oauth2/callback"
}
```

**성공 응답 (200)**
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  }
}
```

**에러 응답 (401)**
```json
{
  "code": "A008",
  "message": "OAuth2 인증에 실패했습니다."
}
```

### 2. 토큰 갱신

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답 (200)**
```json
{
  "code": "SUCCESS",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer"
  }
}
```

### 3. 로그아웃

```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**응답 (200)**
```json
{
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다."
}
```

---

## 테스트 API (개발용)

프론트엔드 없이 로컬에서 OAuth2 로그인을 테스트할 수 있는 API입니다.

### Google 로그인 테스트

**1단계: Google 로그인 페이지로 이동**
```
브라우저에서 접속: http://localhost:8080/api/test/oauth2/google/login
→ Google 로그인 페이지로 자동 리다이렉트
```

**2단계: Google 로그인 완료 후 Authorization Code 확인**
```
Google 로그인 완료 후 자동으로 콜백 페이지로 이동
→ 화면에 code와 redirectUri가 표시됨
```

**3단계: Swagger 또는 Postman으로 로그인 API 테스트**
```http
POST /api/auth/login/google
Content-Type: application/json

{
  "code": "위에서 받은 code 값",
  "redirectUri": "http://localhost:8080/api/test/oauth2/google/callback"
}
```

---

## 환경 설정

### 백엔드 환경변수

```properties
# Google OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# JWT
JWT_SECRET=your-secret-key-minimum-256-bits

# Redis (Refresh Token 저장)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Google Cloud Console 설정

**승인된 리디렉션 URI** (환경별)
- Local (프론트엔드): `http://localhost:3000/oauth2/callback`
- Local (테스트 API): `http://localhost:8080/api/test/oauth2/google/callback`
- QA: `https://qa.cotato.com/oauth2/callback`
- Production: `https://cotato.com/oauth2/callback`

---

## 주요 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| A008 | OAuth2 인증에 실패했습니다 | Google 인증 실패 |
| A009 | 유효하지 않은 JWT 토큰입니다 | 잘못된 토큰 |
| A010 | 만료된 JWT 토큰입니다 | 토큰 만료 |
| A011 | Refresh 토큰이 유효하지 않습니다 | 갱신 실패 |

---

## 보안 고려사항

- **HTTPS 필수**: Production에서는 반드시 HTTPS 사용
- **CORS 설정**: 백엔드에서 프론트엔드 도메인 허용
- **Redirect URI 검증**: Google Console에 등록된 URI만 허용
- **토큰 보관**: Access/Refresh Token 안전하게 저장 (HttpOnly Cookie 권장)
- **테스트 API**: Local 환경에서만 사용, Production에서는 비활성화 권장
