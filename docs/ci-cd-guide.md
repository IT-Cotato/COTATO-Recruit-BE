# CI/CD 자동화 가이드

## 개요

1. **develop merge** → release 브랜치 생성 + **빌드** + QA 배포
2. **release → main merge** → Tag 생성 + **이미지 재사용** + Production 배포

## 버전 관리

날짜 기반 버전: `YYYY.MM.DD.N` (예: `2025.12.01.1`)
- Git Tag: `v` 접두사 추가 (예: `v2025.12.01.1`)
- Docker 이미지: 버전 그대로 (예: `cotato-recruit:2025.12.01.1`)

## 자동화 워크플로우

### 1. QA 배포 (develop → release 브랜치)

**트리거**: develop에 merge

**과정**:
1. `release/2025.12.01.1` 브랜치 자동 생성
2. 빌드 & 테스트
3. Docker 이미지 생성: `cotato-recruit:2025.12.01.1`
4. QA 서버 Blue-Green 배포

### 2. Production 배포 (release → main)

**트리거**: release 브랜치를 main에 merge

**과정**:
1. Git Tag 생성: `v2025.12.01.1`
2. GitHub Release 생성
3. **QA 이미지 재사용**: `2025.12.01.1` → `v2025.12.01.1`, `production-latest`
4. Production 서버 Blue-Green 배포
5. main → develop 역머지
6. **release 브랜치 유지** (롤백용)

## 사용 예시

```bash
# 1. feature 개발 → develop merge
# → release/2025.12.01.1 자동 생성 + 빌드 + QA 배포

# 2. QA 테스트 후 버그 수정
git checkout release/2025.12.01.1
# 수정...
git push  # → QA 재배포

# 3. release → main merge
# → Tag 생성 + QA 이미지 재사용 + Production 배포
```

## 핵심 특징

- **한 번만 빌드**: QA 배포 시에만 빌드/테스트, Production은 동일 이미지 재사용
- **빠른 배포**: Production은 이미지 재태깅만 하므로 빠름
- **안전성**: QA에서 검증된 동일 이미지가 Production에 배포
- **롤백 가능**: release 브랜치 유지로 언제든 재배포 가능
