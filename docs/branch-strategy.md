# Git 브랜치 전략 가이드

## Squash and Merge 전략

### 개념

**Squash and Merge**는 PR의 여러 커밋을 하나의 커밋으로 합쳐서 타겟 브랜치에 병합하는 전략입니다.

**일반 Merge vs Squash and Merge:**

```
일반 Merge:
develop: A --- B --- C --- D --- E --- F
                      \           /
feature:               G --- H --- I
→ feature의 모든 커밋(G, H, I)이 develop에 그대로 남음

Squash and Merge:
develop: A --- B --- C --- D
                            \
                             S (G+H+I가 합쳐진 하나의 커밋)
→ feature의 모든 커밋이 하나로 압축되어 깔끔한 히스토리 유지
```

### 장점

- **깔끔한 히스토리**: develop/main 브랜치가 기능 단위의 커밋으로만 구성됨
- **쉬운 롤백**: 특정 기능을 되돌리기 쉬움 (하나의 커밋만 revert)
- **작업 과정 숨김**: 개발 중 시행착오 커밋들을 하나로 정리

## 브랜치 네이밍 규칙

모든 작업 브랜치는 `타입/작업명` 형식을 따릅니다.

### 1. feature/* - 새로운 기능 개발

**예시:**
- `feature/user-authentication`
- `feature/product-search`
- `feature/payment-integration`

### 2. bugfix/* - 버그 수정

**예시:**
- `bugfix/login-validation-error`
- `bugfix/null-pointer-exception`

### 3. hotfix/* - 긴급 수정

**예시:**
- `hotfix/2025.12.01-hotfix.1` (날짜 기반, 자동 생성)

### 4. chore/* - 설정 및 빌드 관련

**예시:**
- `chore/update-dependencies`
- `chore/configure-ci-pipeline`

### 5. refactor/* - 코드 리팩토링

**예시:**
- `refactor/service-layer-restructure`
- `refactor/optimize-query-performance`

### 6. test/* - 테스트 코드

**예시:**
- `test/add-user-service-tests`
- `test/integration-test-setup`

### 7. docs/* - 문서 작업

**예시:**
- `docs/api-documentation`
- `docs/update-readme`

### 브랜치명 작성 가이드

**DO:**
- 소문자 사용
- 하이픈(-)으로 단어 구분
- 간결하고 명확하게 (3-5 단어)

**DON'T:**
- 대문자 사용 금지
- 언더스코어(_) 사용 금지
- 너무 긴 브랜치명
- 모호한 이름 (fix-bug, update-code 등)

## 브랜치 워크플로우

### 주요 브랜치

- **main**: 운영 서버 배포 브랜치 (실제 운영 중인 코드)
- **develop**: 개발 통합 브랜치 (다음 릴리즈 개발)
- **feature**: 기능 개발 브랜치
- **release**: 릴리즈 준비 브랜치 (QA 테스트)
- **hotfix**: 긴급 버그 수정 브랜치

### 전체 흐름

```
feature/* → develop (Squash Merge)
              ↓
         release/* (GitHub Actions 자동 생성, develop 기준)
              ↓
          QA 서버 배포
              ↓
         main (Squash Merge)
              ↓
    Tag 생성 (main 기준) + Production 배포
              ↓
      main → develop 역머지
```

### 개발 워크플로우

#### 1. Feature 개발

```bash
# develop에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# 개발 작업 (여러 번 커밋 가능)
git add .
git commit -m "feat: add login API"
git commit -m "feat: add token validation"
git commit -m "test: add authentication tests"

# 원격에 푸시
git push origin feature/user-authentication
```

#### 2. Pull Request 및 Merge

1. GitHub에서 `feature/user-authentication` → `develop` PR 생성
2. 코드 리뷰 진행
3. **Squash and Merge**로 병합
4. 브랜치 삭제

#### 3. Release 배포

1. GitHub Actions에서 `Create Release Branch` 워크플로우 실행
2. `release/2025.12.01.1` 브랜치 자동 생성 (develop 기준) 및 QA 서버 배포
3. QA 테스트 진행 (버그 발견 시 release 브랜치에서 수정 → 재배포)
4. QA 완료 후 `release/2025.12.01.1` → `main` PR 생성 및 Merge
5. 자동으로:
   - Tag 생성 (`v2025.12.01.1`, main 기준)
   - Production 서버 배포
   - main → develop 역머지 (release에서 수정한 내용 반영)
   - release 브랜치 삭제

#### 4. Hotfix 배포

1. GitHub Actions에서 `Create Hotfix Branch` 워크플로우 실행
2. `hotfix/2025.12.01-hotfix.1` 브랜치 자동 생성 (main 기준) 및 QA 서버 배포
3. 버그 수정 후 QA 테스트
4. QA 완료 후 `hotfix/*` → `main` Merge
5. 자동으로:
   - Tag 생성 (`v2025.12.01-hotfix.1`, main 기준)
   - Production 서버 배포
   - main → develop 역머지
   - hotfix 브랜치 삭제

## 브랜치 보호 규칙

### main 브랜치

- ✅ Require a pull request before merging
  - Require approvals: 1명 이상
- ✅ Require status checks to pass before merging
- ✅ Require conversation resolution before merging

### develop 브랜치

- ✅ Require a pull request before merging
  - Require approvals: 1명 이상
- ✅ Require status checks to pass before merging

### release/* 브랜치

- ✅ Require a pull request before merging
- ✅ Require status checks to pass before merging

## 주의사항

### ✅ DO
- Feature 브랜치는 항상 `develop`에서 분기
- PR을 통한 코드 리뷰 필수
- Squash and Merge로 히스토리를 깔끔하게 유지
- 의미 있는 커밋 메시지 작성
- Release/Hotfix 브랜치는 GitHub Actions를 통해 생성

### ❌ DON'T
- `main`, `develop`, `release/*` 브랜치에 직접 커밋 금지
- 리뷰 없이 병합 금지
- 너무 큰 단위의 PR 생성 지양
- Release/Hotfix 브랜치를 수동으로 생성하지 말 것

자세한 CI/CD 자동화 내용은 [CI/CD 가이드](./CI-CD-GUIDE.md)를 참고하세요.
