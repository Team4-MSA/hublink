# Git Convention


## 깃허브 커밋 규칙

| 작업 타입 | 작업 내용 |
| --- | --- |
| ✨ update | 해당 파일에 새로운 기능이 생김 |
| 🎉 add | 없던 파일 생성, 초기 세팅 |
| 🐛 bugfix | 버그 수정 |
| ♻️ refactor | 코드 리팩토링 |
| 🩹 fix | 코드 수정 |
| 🚚 move | 파일 이동 및 정리 |
| 🔥 del | 기능 또는 파일 삭제 |
| 🍻 test | 테스트 코드 작성 |
| 💄 style | CSS 및 스타일 수정 |
| 🙈 gitfix | `.gitignore` 수정 |
| 🔨 script | `package.json`, 의존성 등 스크립트 수정 |

---

## 커밋 메시지 컨벤션

### 타입(Type)

| 타입 | 설명 |
| --- | --- |
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 (README, API 명세서 등) |
| style | 코드 포맷팅, 세미콜론 누락 등 코드 변경 없는 수정 |
| refactor | 코드 리팩토링 |
| test | 테스트 코드 추가 및 수정 |
| chore | 빌드 설정, 패키지 매니저 설정 등 |

---

### 커밋 메시지 예시

```text
feat: 주문 생성 API 구현
fix: 배송 상태 업데이트 오류 수정
chore: JWT 의존성 라이브러리 추가
```

---

## 브랜치 전략

### 브랜치 종류

- `main`
- `develop`
- `feature`
- `hotfix`

---

### 브랜치 역할

| 브랜치 | 설명 |
| --- | --- |
| main | 실제 운영 배포 브랜치 |
| develop | 개발 통합 브랜치 |
| feature | 기능 개발 브랜치 |
| hotfix | 긴급 수정 브랜치 |

---

### 브랜치 네이밍 예시

```text
feature/order-create
feature/payment-api
hotfix/jwt-error
```