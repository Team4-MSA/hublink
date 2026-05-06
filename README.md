# msa-project

### 구조

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

### 목표

- MSA 기반 프로젝트의 기본 구조를 구성한다.
- Eureka Server를 통해 각 서비스가 정상적으로 등록되는지 확인한다.
- Config Server를 통해 서비스별 설정 파일을 외부에서 관리한다.
- API Gateway를 통해 요청이 각 서비스로 정상 라우팅되는지 검증한다.
- Actuator의 `/actuator/health`를 활용하여 서비스 상태를 확인한다.
- 우선 여기까지
- Feign Client를 이용해 서비스 간 통신을 구현한다.
- Circuit Breaker를 적용하여 장애 상황에 대비한다.
- Zipkin을 통해 서비스 간 요청 흐름을 추적한다.

### 세팅

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

### 구현

1. Eureka Server를 생성하여 서비스 디스커버리 환경 구성
2. Config Server를 생성하여 각 서비스의 설정 파일을 중앙에서 관리할 수 있도록 구성
3. auth-service, user-service, order-service에 Config Client를 적용하여 외부 설정을 읽도록 설정
4. 각 서비스를 Eureka Discovery Client로 등록하여 Eureka Server 대시보드에서 정상 등록 여부를 확인
5. API Gateway를 구성하여 auth, user, order 서비스로 요청이 정상 라우팅되는지 검증
6. 각 서비스에 Actuator를 적용하고 `/actuator/health` 엔드포인트를 통해 상태 확인
7. OpenFeign을 적용하여 서비스 간 HTTP 호출 구조 구현
8. Circuit Breaker를 적용하여 특정 서비스 장애 시 전체 시스템으로 장애가 확산되지 않도록 구성
    1. order-service에 OpenFeign + Circuit Breaker 적용
9. Zipkin을 적용하여 API Gateway와 각 서비스 간 요청 흐름을 분산 추적할 수 있도록 설정
    1. api-gateway, auth-service, user-service, order-service에 적용

1차로 6번까지 구현 후 각 서비스에 openfeign, 서킷브레이커, 분산 추적 등을 추가로 적용할 예정
