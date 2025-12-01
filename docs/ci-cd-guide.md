# CI/CD 자동화 가이드

## 개요

이 프로젝트는 다음과 같은 배포 자동화 흐름을 따릅니다:

1. Feature 브랜치에서 기능 개발 후 `develop` 브랜치에 Merge
2. **자동화 1,2**: GitHub Actions를 통해 새로운 Release/Hotfix 브랜치 생성 및 버전 자동 생성
3. **자동화 3**: Release/Hotfix 브랜치에 Push 시 QA 서버 자동 배포
4. **자동화 4**: Release/Hotfix 브랜치를 `main`에 Merge 시 Tag 및 GitHub Release 자동 생성
5. **자동화 5**: Tag 생성 시 Production 서버 자동 배포 및 main → develop 동기화

## 버전 관리

### 버전 형식

날짜 기반 버전 관리를 사용합니다:

- **Release**: `YYYY.MM.DD.N` (예: `2025.12.01.1`, `2025.12.01.2`)
  - 같은 날짜에 여러 번 생성 시 일련번호 자동 증가
- **Hotfix**: `YYYY.MM.DD-hotfix.N` (예: `2025.12.01-hotfix.1`)

### Git Tag 형식

- Tag는 버전 앞에 `v` 접두사를 붙입니다
- 예: `v2025.12.01.1`, `v2025.12.01.2`, `v2025.12.01-hotfix.1`

## 자동화 워크플로우

### 1. Release 브랜치 생성

새로운 릴리즈를 준비할 때 사용합니다.

**실행 방법**:
1. GitHub 저장소의 `Actions` 탭으로 이동
2. `Create Release Branch` 워크플로우 선택
3. `Run workflow` 클릭
4. 버전 타입 선택:
   - `auto`: 현재 날짜 기반 자동 생성 (예: 2025.12.01.1)
     - 같은 날짜에 이미 release가 있으면 일련번호 자동 증가 (2025.12.01.2)
   - `custom`: 사용자 정의 버전 입력
5. `Run workflow` 실행

**자동 처리 내용**:
- `develop` 브랜치에서 `release/YYYY.MM.DD.N` 브랜치 생성
- `build.gradle`의 버전 자동 업데이트
- `main`으로 향하는 Pull Request 자동 생성

### 2. Hotfix 브랜치 생성

긴급 버그 수정이 필요할 때 사용합니다.

**실행 방법**:
1. GitHub 저장소의 `Actions` 탭으로 이동
2. `Create Hotfix Branch` 워크플로우 선택
3. `Run workflow` 클릭
4. 옵션 선택:
   - `base_branch`: 기준 브랜치 (develop 또는 main)
   - `version_type`: auto 또는 custom
   - `description`: Hotfix 설명 입력
5. `Run workflow` 실행

**자동 처리 내용**:
- 선택한 기준 브랜치에서 `hotfix/YYYY.MM.DD-hotfix.N` 브랜치 생성
- 같은 날짜의 hotfix가 있을 경우 번호 자동 증가
- `build.gradle`의 버전 자동 업데이트
- 기준 브랜치로 향하는 Pull Request 자동 생성

### 3. QA 서버 자동 배포

Release/Hotfix 브랜치에 코드를 Push하면 자동으로 QA 서버에 배포됩니다.

**트리거**:
- `release/*` 및 `hotfix/*` 브랜치에 push
- 수동 실행 (workflow_dispatch)

### 4. Tag 및 Release 자동 생성

Release/Hotfix 브랜치를 `main`에 Merge하면 자동으로 Tag와 GitHub Release가 생성됩니다.

**트리거**:
- `release/*` 또는 `hotfix/*` 브랜치의 Pull Request가 `main`에 merge될 때

**자동 처리 내용**:
- Git Tag 생성 (예: `v2025.12.01`)
- GitHub Release 생성 (Changelog 포함)
- Merge된 브랜치 자동 삭제

**Release Notes 포함 내용**:
- 커밋 목록
- PR 번호 및 제목
- Merge한 사용자
- Merge 시간

## 실제 사용 예시

### 시나리오 1: 새로운 기능 릴리즈

1. **기능 개발**
   ```bash
   git checkout -b feature/user-authentication
   # 개발 작업...
   git push origin feature/user-authentication
   ```

2. **develop에 Merge**
   - GitHub에서 PR 생성 및 Merge

3. **Release 브랜치 생성**
   - GitHub Actions에서 `Create Release Branch` 워크플로우 실행
   - `auto` 버전 선택
   - 자동으로 `release/2025.12.01.1` 브랜치 생성됨
   - 같은 날 2번째 release 생성 시 자동으로 `release/2025.12.01.2`로 생성

4. **QA 테스트**
   - `release/2025.12.01.1` 브랜치에 push 시 QA 서버 자동 배포
   - QA 테스트 수행
   - 버그 발견 시 `release/2025.12.01.1` 브랜치에서 수정 후 push
   - 수정 후 자동으로 재배포됨

5. **릴리즈 완료 및 운영 배포**
   - QA 테스트 완료 후 PR을 `main`에 Merge
   - 자동으로:
     - `v2025.12.01.1` Tag 생성 (main 기준)
     - GitHub Release 자동 생성
     - **Production 서버 자동 배포**
     - main → develop 역머지 (release에서 수정한 내용 반영)
     - `release/2025.12.01.1` 브랜치 자동 삭제

### 시나리오 2: 긴급 버그 수정 (Hotfix)

1. **Hotfix 브랜치 생성**
   - GitHub Actions에서 `Create Hotfix Branch` 워크플로우 실행
   - `base_branch`: main (운영 서버 기준)
   - `version_type`: auto
   - `description`: "로그인 오류 수정"
   - 자동으로 `hotfix/2025.12.01-hotfix.1` 브랜치 생성됨

2. **버그 수정**
   ```bash
   git checkout hotfix/2025.12.01-hotfix.1
   # 버그 수정...
   git push origin hotfix/2025.12.01-hotfix.1
   ```
   - Push 시 QA 서버 자동 배포

3. **Hotfix 완료 및 운영 배포**
   - QA 테스트 완료 후 PR을 `main`에 Merge
   - 자동으로:
     - `v2025.12.01-hotfix.1` Tag 생성 (main 기준)
     - GitHub Release 자동 생성
     - **Production 서버 자동 배포**
     - main → develop 역머지
     - `hotfix/2025.12.01-hotfix.1` 브랜치 자동 삭제

## 문제 해결

### Release 브랜치가 이미 존재하는 경우

같은 버전의 Release 브랜치가 이미 있다면 워크플로우가 실패합니다.
- `custom` 버전 타입을 선택하여 다른 버전으로 생성하거나
- 기존 브랜치를 삭제 후 다시 생성

### QA 배포 실패 시

1. GitHub Actions 로그 확인
2. QA 서버 상태 확인
3. Docker Hub 접근 권한 확인
4. Secrets 설정 확인

### Tag 생성 실패 시

1. 같은 Tag가 이미 존재하는지 확인
2. PR이 올바르게 merge되었는지 확인
3. 브랜치 이름이 `release/*` 또는 `hotfix/*` 형식인지 확인

## Production 서버 자동 배포

Release/Hotfix 브랜치가 main에 Merge되어 Tag가 생성되면 자동으로 Production 서버에 배포됩니다.

### 배포 트리거

- Tag 생성 시 자동 실행 (예: `v2025.12.01` Push 시)
- 수동 실행도 가능 (GitHub Actions에서 특정 Tag 선택)

### Production 배포 실패 시

1. GitHub Actions 로그 확인
2. Production 서버 상태 확인
3. Docker Hub 접근 권한 확인
4. Production Secrets 설정 확인
5. 롤백이 필요한 경우 이전 Tag로 수동 재배포
