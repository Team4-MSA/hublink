# msa-project

### 🍫 구조

```bash
Team4-MSA
└── msa
    ├── eureka-server   # 19090
    ├── api-gateway     # 19091
    ├── auth-service    # 19092
    ├── user-service    # 19093
    ├── order-service   # 19094
    ├── config-server   # 19095
    └── config-repo
        ├── api-gateway.yml
        ├── auth-service.yml
        ├── user-service.yml
        └── order-service.yml
```

### 🌱 목표

- MSA 기반 프로젝트의 기본 구조를 구성한다.
- Eureka Server를 통해 각 서비스가 정상적으로 등록되는지 확인한다.
- Config Server를 통해 서비스별 설정 파일을 외부에서 관리한다.
- API Gateway를 통해 요청이 각 서비스로 정상 라우팅되는지 검증한다.
- Actuator의 `/actuator/health`를 활용하여 서비스 상태를 확인한다.
- 우선 여기까지
- 인증 책임과 사용자 관리 책임을 분리하기 위해 `auth-service`와 `user-service`를 별도 서비스로 구성한다.
- Feign Client를 이용해 서비스 간 통신을 구현한다.
- Circuit Breaker를 적용하여 장애 상황에 대비한다.
- Zipkin을 통해 서비스 간 요청 흐름을 추적한다.

### ⚙️ 세팅

- Spring Boot 3.5.14
- Java 17
- Gradle
- YAML 설정 파일 사용
- Eureka Server
- Config Server
- API Gateway
- Eureka Discovery Client
- Spring Cloud Config Client
- Spring Boot Actuator
    - `/actuator/health`를 통한 서비스 상태 확인
- OpenFeign
- Circuit Breaker
- Zipkin


### 🐥 구현

1. Eureka Server를 생성하여 서비스 디스커버리 환경 구성
2. Config Server를 생성하여 각 서비스의 설정 파일을 중앙에서 관리할 수 있도록 구성
3. auth-service, user-service, order-service에 Config Client를 적용하여 외부 설정을 읽도록 설정
4. 각 서비스를 Eureka Discovery Client로 등록하여 Eureka Server 대시보드에서 정상 등록 여부를 확인
5. API Gateway를 구성하여 auth, user, order 서비스로 요청이 정상 라우팅되는지 검증
6. 각 서비스에 Actuator를 적용하고 `/actuator/health` 엔드포인트를 통해 상태 확인
7. ~~OpenFeign을 적용하여 서비스 간 HTTP 호출 구조 구현~~
8. ~~Circuit Breaker를 적용하여 특정 서비스 장애 시 전체 시스템으로 장애가 확산되지 않도록 구성~~
    1. ~~order-service에 OpenFeign + Circuit Breaker 적용~~
9. ~~Zipkin을 적용하여 API Gateway와 각 서비스 간 요청 흐름을 분산 추적할 수 있도록 설정~~
    1. ~~api-gateway, auth-service, user-service, order-service에 적용~~

1차로 6번까지 구현 후 각 서비스에 openfeign, 서킷브레이커, 분산 추적 등을 추가로 적용할 예정
<img width="2550" height="1392" alt="image" src="https://github.com/user-attachments/assets/f82e1316-4f99-45a8-a001-f6d1495bcbcd" />


### 🥨 인증 서버 분리 기준

인증 기능은 `auth-service`로 분리하여 관리한다.

- `auth-service`
    - 로그인
    - JWT 발급
    - 토큰 검증
    - 인증 관련 API 관리
- `user-service`
    - 회원가입
    - 회원 정보 조회
    - 회원 정보 수정
    - 회원 탈퇴

인증 책임과 사용자 도메인 책임을 분리하여 서비스 간 역할을 명확히 하고, 이후 Gateway에서 JWT 검증 구조와 연계할 수 있도록 확장 가능하게 구성한다.

### 🧊 common 모듈 관리 기준

`common` 모듈은 서비스 간 결합도를 높이지 않도록 최소 범위로 관리한다.

포함 대상:

- 공통 응답 포맷
- 공통 에러 응답 구조
- 비즈니스와 무관한 공통 유틸
- 로그 추적용 공통 코드

제외 대상:

- Entity
- Repository
- Service
- 도메인 DTO
- 도메인별 ErrorCode
- 특정 서비스의 비즈니스 로직

도메인과 직접 관련된 코드는 각 서비스 내부에서 관리하고, `common` 모듈은 여러 서비스에서 반복되는 비즈니스 비의존 코드만 포함한다.

### 📍 추후 적용 예정

아래 기능은 도메인 API가 구체화된 이후 적용할 예정이다.

- OpenFeign을 이용한 서비스 간 HTTP 통신
- Circuit Breaker를 통한 장애 전파 방지
- Zipkin을 통한 분산 추적

현재 단계에서는 각 서비스의 도메인 기능이 충분히 구현되지 않았기 때문에, Feign Client, Circuit Breaker, Zipkin은 우선 제외하고 기본 MSA 구조 검증에 집중한다.
