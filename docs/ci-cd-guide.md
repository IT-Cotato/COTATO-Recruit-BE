# CI/CD 자동화 가이드

## 개요

이 프로젝트는 완전 자동화된 배포 시스템을 사용합니다:

1. **develop merge** → release 브랜치 자동 생성 + QA 배포
2. **release/hotfix → main merge** → Tag 생성 + Production 배포 + develop 역머지

## 버전 관리

### 버전 형식

날짜 기반 버전 관리를 사용합니다:

- **Release**: `YYYY.MM.DD.N` (예: `2025.12.01.1`)
  - 같은 날짜에 여러 번 생성 시 일련번호 자동 증가
- **Hotfix**: `YYYY.MM.DD-hotfix.N` (예: `2025.12.01-hotfix.1`)
- **Git Tag**: 버전 앞에 `v` 접두사 (예: `v2025.12.01.1`)

## 자동화 워크플로우

### 1. Release 자동 생성 (develop merge 시)

**트리거**: feature 브랜치가 develop에 merge될 때

**자동 처리**:
- `release/YYYY.MM.DD.N` 브랜치 생성 (develop 기준)
- `build.gradle` 버전 자동 업데이트
- QA 서버 자동 배포 (Blue-Green 무중단)

**워크플로우**: `.github/workflows/auto-release-on-develop-merge.yml`

### 2. Hotfix 생성 (수동)

**실행 방법**:
1. GitHub Actions → `Create Hotfix Branch` 워크플로우 실행
2. 옵션 입력:
   - `base_branch`: main (보통)
   - `version_type`: auto (권장)
   - `description`: Hotfix 설명

**자동 처리**:
- `hotfix/YYYY.MM.DD-hotfix.N` 브랜치 생성
- `build.gradle` 버전 자동 업데이트
- QA 서버 자동 배포

**워크플로우**: `.github/workflows/create-hotfix-branch.yml`

### 3. QA 서버 배포 (Blue-Green)

**트리거**: `release/*`, `hotfix/*` 브랜치에 push

**배포 과정**:
1. 빌드 및 테스트
2. Docker 이미지 생성 및 푸시
3. Blue-Green 무중단 배포:
   - 유휴 컨테이너에 새 버전 배포
   - 헬스 체크 (최대 150초)
   - 트래픽 전환
   - 이전 컨테이너 종료

**워크플로우**: `.github/workflows/deploy-qa.yml`

### 4. Production 배포 (main merge 시)

**트리거**: release/hotfix 브랜치가 main에 merge될 때

**자동 처리**:
1. Git Tag 생성 (예: `v2025.12.01.1`)
2. GitHub Release 생성
3. Production 서버 Blue-Green 무중단 배포
4. main → develop 자동 역머지
5. release/hotfix 브랜치 자동 삭제

**워크플로우**:
- `.github/workflows/create-tag-and-release.yml`
- `.github/workflows/deploy-production.yml`

## 실제 사용 예시

### 새로운 기능 릴리즈

```bash
# 1. 기능 개발
git checkout -b feature/user-authentication
# 개발 작업...
git push origin feature/user-authentication

# 2. develop에 PR 생성 및 Merge
# → 자동으로 release 브랜치 생성 및 QA 배포됨

# 3. QA 테스트
# 버그 발견 시:
git checkout release/2025.12.01.1
# 버그 수정...
git push  # → QA 재배포

# 4. release → main PR 생성 및 Merge
# → 자동으로 Tag 생성, Production 배포, develop 역머지
```

### 긴급 버그 수정 (Hotfix)

```bash
# 1. GitHub Actions에서 Hotfix 브랜치 생성
# → 자동으로 hotfix 브랜치 생성 및 QA 배포됨

# 2. 버그 수정
git checkout hotfix/2025.12.01-hotfix.1
# 수정...
git push  # → QA 재배포

# 3. hotfix → main PR 생성 및 Merge
# → 자동으로 Tag 생성, Production 배포, develop 역머지
```

## 문제 해결

### QA 배포 실패 시
1. GitHub Actions 로그 확인
2. QA 서버 상태 및 Docker 컨테이너 확인
3. Secrets 설정 확인

### Production 배포 실패 시
1. GitHub Actions 로그 확인
2. 롤백 필요 시: GitHub Actions에서 이전 Tag로 수동 재배포

### Tag 생성 실패 시
1. 같은 Tag가 이미 존재하는지 확인
2. 브랜치 이름이 `release/*` 또는 `hotfix/*` 형식인지 확인
