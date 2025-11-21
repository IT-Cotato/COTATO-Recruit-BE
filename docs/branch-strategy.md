# Git 브랜치 전략 가이드

## 개요

본 프로젝트는 **Squash and Merge** 전략을 사용하여 코드 통합을 진행합니다.
명확한 브랜치 구조와 워크플로우를 통해 안정적인 배포와 효율적인 협업을 추구합니다.

## Squash and Merge란?

### 개념

**Squash and Merge**는 PR의 여러 커밋을 하나의 커밋으로 합쳐서 타겟 브랜치에 병합하는 전략입니다.

### 일반 Merge vs Squash and Merge

**일반 Merge (Merge Commit):**
```
develop: A --- B --- C --- D --- E --- F
                      \           /
feature:               G --- H --- I
```
- feature 브랜치의 모든 커밋(G, H, I)이 develop에 그대로 남음
- 커밋 히스토리가 복잡해질 수 있음

**Squash and Merge:**
```
develop: A --- B --- C --- D
                            \
                             S (G+H+I가 합쳐진 하나의 커밋)
```
- feature 브랜치의 모든 커밋(G, H, I)이 하나의 커밋(S)로 압축
- 깔끔한 커밋 히스토리 유지

### 장점

1. **깔끔한 히스토리**: develop 브랜치가 기능 단위의 커밋으로만 구성됨
2. **의미 있는 커밋**: 각 커밋이 완성된 기능을 나타냄
3. **쉬운 롤백**: 특정 기능을 되돌리기 쉬움 (하나의 커밋만 revert)
4. **작업 과정 숨김**: 개발 중 시행착오 커밋들을 하나로 정리

### 예시

**Feature 브랜치 작업 중 커밋:**
```
feat: 사용자 인증 API 추가
fix: 토큰 검증 로직 버그 수정
refactor: 코드 리뷰 반영
test: 테스트 케이스 추가
fix: 오타 수정
docs: API 문서 작성
```

**Squash and Merge 후 develop 브랜치:**
```
feat: 사용자 인증 기능 구현 (#123)

- JWT 기반 토큰 인증 추가
- 로그인/로그아웃 API 구현
- 토큰 갱신 로직 추가
- 단위 테스트 및 문서 작성
```

### 언제 사용하나?

- Feature 브랜치 → develop 병합 시 (권장)
- 작은 기능 단위 작업이 완료되었을 때
- 여러 번의 시행착오 커밋을 하나로 정리하고 싶을 때

## 브랜치 구조

### 1. main 브랜치
- **목적**: 운영 중인 프로덕션 환경의 코드
- **특징**:
  - 항상 배포 가능한 안정적인 상태 유지
  - 실제 서비스에서 실행 중인 코드
  - 직접 커밋 금지
- **병합 소스**: `release` 브랜치에서만 병합 가능

### 2. release 브랜치
- **목적**: 배포 전 QA(Quality Assurance) 진행
- **특징**:
  - 실제 유저에게 배포하기 전 최종 검증 단계
  - QA 팀의 테스트 수행
  - 버그 픽스만 허용
  - 직접 커밋 금지
- **병합 소스**: `develop` 브랜치에서만 병합 가능
- **병합 대상**: QA 완료 후 `main` 브랜치로 병합

### 3. develop 브랜치
- **목적**: 개발이 진행 중인 통합 브랜치
- **특징**:
  - 다음 릴리즈를 위한 개발 작업 통합
  - 기능 개발의 기본 브랜치
  - 직접 커밋 금지
- **병합 소스**: `feature/*` 브랜치에서 PR을 통해 병합
- **병합 대상**: 릴리즈 준비 완료 시 `release` 브랜치로 병합

## 브랜치 네이밍 규칙

모든 작업 브랜치는 `타입/작업명` 형식을 따릅니다.
작업명은 영어 소문자와 하이픈(-)을 사용하여 간결하고 명확하게 작성합니다.

### 1. feature/* - 새로운 기능 개발

**목적**: 새로운 기능이나 개선 사항을 개발할 때 사용

**언제 사용하나?**
- 새로운 API 엔드포인트 추가
- 새로운 페이지나 컴포넌트 개발
- 기존에 없던 기능 추가

**예시:**
- `feature/user-authentication` - 사용자 인증 기능
- `feature/product-search` - 상품 검색 기능
- `feature/payment-integration` - 결제 연동
- `feature/email-notification` - 이메일 알림 기능

### 2. bugfix/* - 버그 수정

**목적**: develop 또는 release 브랜치에서 발견된 버그를 수정할 때 사용

**언제 사용하나?**
- QA 과정에서 발견된 버그
- develop 브랜치의 기능 오류
- release 브랜치의 버그 수정

**예시:**
- `bugfix/login-validation-error` - 로그인 검증 오류 수정
- `bugfix/null-pointer-exception` - NPE 오류 수정
- `bugfix/incorrect-total-calculation` - 총합 계산 오류 수정

### 3. hotfix/* - 긴급 수정

**목적**: 프로덕션(main)에서 발견된 치명적인 버그를 긴급하게 수정할 때 사용

**언제 사용하나?**
- 서비스 장애를 유발하는 버그
- 보안 취약점 발견
- 데이터 손실 위험이 있는 오류

**특징:**
- main 브랜치에서 직접 분기
- 최우선 순위로 처리
- main과 develop 모두에 병합 필요

**예시:**
- `hotfix/payment-critical-bug` - 결제 시스템 치명적 버그
- `hotfix/security-vulnerability` - 보안 취약점 수정
- `hotfix/data-loss-prevention` - 데이터 손실 방지

### 4. chore/* - 설정 및 빌드 관련

**목적**: 코드 변경 없이 설정, 빌드, 패키지 관련 작업을 할 때 사용

**언제 사용하나?**
- 의존성 패키지 업데이트
- 빌드 스크립트 수정
- CI/CD 파이프라인 설정
- 환경 설정 파일 수정
- .gitignore 등 설정 파일 변경

**예시:**
- `chore/update-dependencies` - 의존성 업데이트
- `chore/configure-ci-pipeline` - CI 파이프라인 설정
- `chore/add-docker-compose` - Docker Compose 추가
- `chore/update-gradle-version` - Gradle 버전 업데이트

### 5. refactor/* - 코드 리팩토링

**목적**: 기능 변경 없이 코드 구조를 개선할 때 사용

**언제 사용하나?**
- 코드 구조 개선
- 성능 최적화
- 중복 코드 제거
- 디자인 패턴 적용

**예시:**
- `refactor/service-layer-restructure` - 서비스 레이어 재구조화
- `refactor/optimize-query-performance` - 쿼리 성능 최적화
- `refactor/remove-duplicate-code` - 중복 코드 제거
- `refactor/apply-strategy-pattern` - 전략 패턴 적용

### 6. test/* - 테스트 코드

**목적**: 테스트 코드를 추가하거나 수정할 때 사용

**언제 사용하나?**
- 단위 테스트 추가
- 통합 테스트 작성
- E2E 테스트 구현
- 테스트 커버리지 개선

**예시:**
- `test/add-user-service-tests` - 사용자 서비스 테스트 추가
- `test/integration-test-setup` - 통합 테스트 설정
- `test/e2e-payment-flow` - 결제 플로우 E2E 테스트

### 7. docs/* - 문서 작업

**목적**: 문서를 추가하거나 수정할 때 사용

**언제 사용하나?**
- README 작성/수정
- API 문서 작성
- 아키텍처 문서 작성
- 주석 개선

**예시:**
- `docs/api-documentation` - API 문서 작성
- `docs/update-readme` - README 업데이트
- `docs/architecture-diagram` - 아키텍처 다이어그램 추가
- `docs/contributing-guide` - 기여 가이드 작성

### 8. deploy/* - 배포 관련

**목적**: 배포 스크립트나 배포 설정을 추가/수정할 때 사용

**언제 사용하나?**
- 배포 스크립트 작성
- 배포 자동화 설정
- 인프라 코드 작성 (IaC)
- 서버 설정 변경

**예시:**
- `deploy/add-kubernetes-manifest` - Kubernetes 매니페스트 추가
- `deploy/configure-nginx` - Nginx 설정
- `deploy/setup-monitoring` - 모니터링 설정
- `deploy/blue-green-deployment` - Blue-Green 배포 설정

### 9. style/* - 코드 스타일

**목적**: 코드 포맷팅, 스타일 변경 (동작 변경 없음)

**언제 사용하나?**
- 코드 포맷터 적용
- 린터 규칙 적용
- 들여쓰기 수정
- 세미콜론, 공백 등 스타일 통일

**예시:**
- `style/apply-prettier` - Prettier 포맷팅 적용
- `style/fix-indentation` - 들여쓰기 수정
- `style/apply-eslint-rules` - ESLint 규칙 적용

### 브랜치명 작성 가이드

**DO:**
- 소문자 사용
- 하이픈(-)으로 단어 구분
- 간결하고 명확하게 (3-5 단어)
- 동사 사용 권장

**DON'T:**
- 대문자 사용 금지
- 언더스코어(_) 사용 금지
- 너무 긴 브랜치명
- 모호한 이름 (fix-bug, update-code 등)

---

## 개발 워크플로우

### 전체 흐름 다이어그램

```
develop
   │
   ├─── feature/user-login
   │         │
   │      (개발 진행)
   │      - feat: 로그인 API 추가
   │      - fix: 버그 수정
   │      - test: 테스트 추가
   │         │
   │    ┌────┘
   │    │ PR 생성 및 코드 리뷰
   │    │ Squash and Merge
   │◄───┘ (모든 커밋이 하나로 압축)
   │
   ├─── bugfix/validation-error
   │         │
   │      (버그 수정)
   │         │
   │◄───────┘ (Squash and Merge)
   │
```

### 1. 브랜치 생성

```bash
# develop 브랜치에서 최신 코드 받기
git checkout develop
git pull origin develop

# 작업 브랜치 생성 (위의 네이밍 규칙 참고)
git checkout -b feature/기능명
# 또는
git checkout -b bugfix/버그명
# 또는
git checkout -b chore/작업명
```

### 2. 기능 개발

```bash
# 작업 진행
git add .
git commit -m "feat: 기능 설명"

# 여러 번 커밋 가능 (Squash로 합쳐질 예정)
git commit -m "feat: 추가 작업"
git commit -m "fix: 버그 수정"
git commit -m "refactor: 코드 리팩토링"
```

**커밋 메시지 컨벤션:**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `test`: 테스트 코드 추가/수정
- `docs`: 문서 수정
- `chore`: 빌드 업무 수정, 패키지 매니저 수정 등

### 3. 원격 저장소에 푸시

```bash
git push origin feature/기능명
```

### 4. Pull Request 생성

1. GitHub에서 `feature/기능명` → `develop` 브랜치로 PR 생성
2. PR 제목: 간결하고 명확하게 작성
3. PR 설명:
   - 작업 내용 요약
   - 변경 사항 설명
   - 관련 이슈 번호 (있는 경우)
   - 스크린샷 (UI 변경 시)

### 5. 코드 리뷰

- 최소 1명 이상의 리뷰어 승인 필요
- 리뷰 의견 반영 후 재요청

### 6. Squash and Merge

- PR 승인 완료 후 **Squash and Merge** 수행
- 여러 개의 커밋이 하나의 커밋으로 통합됨
- Merge 커밋 메시지는 의미 있게 작성

```
✅ Squash and Merge 완료 → develop 브랜치에 하나의 커밋으로 통합
```

### 7. 브랜치 정리

```bash
# 로컬 브랜치 삭제
git checkout develop
git pull origin develop
git branch -d feature/기능명

# 원격 브랜치는 GitHub에서 자동 삭제 설정 권장
```

## 릴리즈 프로세스

### 1. develop → release

```bash
# release 브랜치 생성 (최초 1회)
git checkout -b release
git push origin release

# 또는 기존 release 브랜치에 병합
git checkout release
git pull origin release
git merge develop
git push origin release
```

### 2. QA 진행

- QA 팀에서 `release` 브랜치의 코드 테스트
- 버그 발견 시 `bugfix/*` 브랜치에서 수정 후 `release`로 병합
- 심각한 버그는 `develop` 브랜치에도 병합

### 3. release → main

```bash
# QA 완료 후 main 브랜치로 배포
git checkout main
git pull origin main
git merge release
git push origin main

# 태그 생성 (버전 관리)
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 4. main → develop 동기화

```bash
# main의 변경사항을 develop에도 반영
git checkout develop
git pull origin develop
git merge main
git push origin develop
```

## 주의사항

### ✅ DO
- Feature 브랜치는 항상 `develop`에서 분기
- PR을 통한 코드 리뷰 필수
- Squash and Merge로 히스토리를 깔끔하게 유지
- 의미 있는 커밋 메시지 작성
- 작은 단위로 자주 커밋

### ❌ DON'T
- `main`, `release`, `develop` 브랜치에 직접 커밋 금지
- 리뷰 없이 병합 금지
- 너무 큰 단위의 PR 생성 지양
- 다른 사람의 feature 브랜치에 직접 푸시 금지

## 긴급 수정 (Hotfix)

프로덕션 환경의 긴급한 버그 수정이 필요한 경우:

```bash
# main에서 hotfix 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/긴급수정명

# 수정 작업
git add .
git commit -m "hotfix: 긴급 버그 수정"
git push origin hotfix/긴급수정명

# main과 develop 모두에 병합
# 1. main으로 PR 생성 및 병합
# 2. develop으로도 PR 생성 및 병합
```

## 브랜치 보호 규칙 (GitHub Settings)

GitHub 저장소의 Settings → Branches → Branch protection rules에서 아래 규칙을 설정합니다.

### main 브랜치

#### 1. Require a pull request before merging (병합 전 Pull Request 필수)
- ✅ 체크 필수
- **Require approvals**: 최소 승인 수를 `1`로 설정
  - 최소 1명 이상의 리뷰어 승인이 있어야 병합 가능
- **Dismiss stale pull request approvals when new commits are pushed** (선택사항)
  - 새로운 커밋이 push되면 기존 승인 무효화

#### 2. Require status checks to pass before merging (병합 전 상태 체크 통과 필수)
- ✅ 체크 필수
- **Require branches to be up to date before merging**
  - ✅ 체크 필수
  - CI/CD 테스트가 모두 통과해야 병합 가능
  - 타겟 브랜치의 최신 변경사항을 반영해야 병합 가능

#### 3. Require conversation resolution before merging (병합 전 대화 해결 필수)
- ✅ 체크 권장
- 모든 리뷰 코멘트가 해결되어야 병합 가능

#### 4. Do not allow bypassing the above settings (위 설정 우회 불가)
- ✅ 체크 필수
- 관리자도 위의 보호 규칙을 따라야 함
- **Include administrators** 옵션 체크

#### 5. Restrict who can push to matching branches (브랜치 푸시 제한)
- ✅ 체크 권장
- 특정 팀이나 사용자만 직접 푸시 가능하도록 제한

---

### release 브랜치

#### 1. Require a pull request before merging
- ✅ 체크 필수
- **Require approvals**: 최소 승인 수를 `1`로 설정

#### 2. Require status checks to pass before merging
- ✅ 체크 필수
- **Require branches to be up to date before merging** ✅ 체크 필수

#### 3. Require conversation resolution before merging
- ✅ 체크 권장

---

### develop 브랜치

#### 1. Require a pull request before merging
- ✅ 체크 필수
- **Require approvals**: 최소 승인 수를 `1`로 설정

#### 2. Require status checks to pass before merging
- ✅ 체크 필수
- **Require branches to be up to date before merging** ✅ 체크 필수

#### 3. Require conversation resolution before merging
- ✅ 체크 권장

---

### 설정 방법 (단계별)

1. **GitHub 저장소 페이지 이동**
2. **Settings** 탭 클릭
3. 좌측 메뉴에서 **Branches** 클릭
4. **Add branch protection rule** 버튼 클릭
5. **Branch name pattern**에 브랜치명 입력
   - `main` 또는 `develop` 또는 `release`
6. 위의 규칙에 맞게 옵션 체크
   - ✅ **Require a pull request before merging**
     - Require approvals: `1`
   - ✅ **Require status checks to pass before merging**
     - ✅ Require branches to be up to date before merging
   - ✅ **Require conversation resolution before merging** (권장)
   - ✅ **Do not allow bypassing the above settings** (main 브랜치)
     - ✅ Include administrators
7. 페이지 하단의 **Create** 또는 **Save changes** 버튼 클릭
8. 각 브랜치(main, release, develop)에 대해 1~7단계 반복