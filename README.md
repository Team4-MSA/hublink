# 🚛 HubLink 

> 🌱 MSA 기반 물류 · 배송 관리 플랫폼

HubLink는 주문, 배송, 허브, 업체, 상품, 재고, 결제, 알림 기능을 독립적인 마이크로서비스로 분리한 물류·배송 관리 플랫폼입니다.

Spring Cloud Gateway, Eureka, Config Server를 기반으로 서비스 라우팅과 디스커버리, 중앙 설정 관리를 구성하였으며, Redis Streams, Kafka를 활용한 이벤트 기반 처리 구조를 적용했습니다.

또한 Docker Compose 기반 로컬 실행 환경과 AWS EC2, RDS, ECR, ALB 기반 배포 구조를 구성하여 서비스별 독립 배포와 운영 확장성을 고려했습니다.

---
# 🎯 프로젝트 목표

- HubLink는 MSA 환경에서 주문, 재고, 배송 등 서비스 간에 발생하는 분산 트랜잭션 문제를 직접 다루는 것을 목표로 한다.
- 주문 생성 및 재고 차감 과정에서는 동시성 충돌이 발생할 수 있으므로, 락 전략과 트랜잭션 처리 방식을 적용하여 데이터 정합성을 보장한다.
- 또한 Saga Orchestration 기반의 트랜잭션 관리 방식을 적용하여 주문, 재고, 배송 서비스 간 작업 흐름을 제어하고, 실패 발생 시 보상 트랜잭션을 통해 일관성을 유지한다.
- 외부 API 호출 실패, 메시지 처리 실패, 특정 서비스 장애가 발생하더라도 전체 시스템이 중단되지 않도록 실패 상태를 저장하고 재처리 가능한 구조를 구성한다.
- 메시징 환경에서 동일 이벤트가 중복 소비되더라도 동일 작업이 반복 수행되지 않도록 멱등성 처리를 적용한다.

> 최종적으로 단순히 서비스를 분리하는 데 그치지 않고, 독립 배포, 장애 격리, 이벤트 기반 통신, 동시성 제어, 모니터링을 직접 구현하며 MSA의 장점과 한계를 함께 이해하는 것을 목표로 한다.
---
# 👥 역할 분담

| 이름  | 담당 영역                                      |
| --- | ------------------------------------------ |
| 이성근 | `user-service`, 인증/권한 연동, Redis, CI/CD     |
| 김신영 | `hub-service`, `company-service`, ArchUnit |
| 최준근 | `product-service`, `stock-service`, Kafka  |
| 김영욱 | `order-service`, Kafka                     |
| 박성우 | `delivery-service`, 분산락, Kafka             |
| 조혜은 | `ai-service`, `slack-service`, 배포          |

---
# 🐥 주요 기능

| 서비스 | 주요 기능 |
|---|---|
| User Service | 회원가입, 로그인, JWT 인증, 권한 관리 |
| Company Service | 업체 등록 및 업체 정보 관리 |
| Hub Service | 허브 등록, 허브 간 이동 경로 관리 |
| Product Service | 상품 등록, 상품 조회, 상품 정보 관리 |
| Stock Service | 재고 관리, 재고 차감 및 복구 |
| Order Service | 주문 생성, 주문 조회, 주문 상태 관리, 주문 취소 |
| Delivery Service | 배송 생성, 배송 상태 변경, 허브 이동 추적 |
| Slack Service | Slack 알림 전송, 이벤트 기반 메시지 처리 |
| AI Service | AI 기반 발송 시한 생성 및 메시지 생성 |
| API Gateway | 인증 필터 처리, 서비스 라우팅 |
| Eureka / Config Server | 서비스 디스커버리 및 중앙 설정 관리 |

---

# 🍟 시스템 아키텍처

<img width="4671" height="2973" alt="인프라설계도 drawio" src="https://github.com/user-attachments/assets/9418f26b-eddb-499e-9a17-af4eb8dcf4d1" />



---

# 🍒 ERD

<img width="2867" height="2062" alt="HubLink" src="https://github.com/user-attachments/assets/97ed965f-cb96-4dc4-930b-aa519d596b69" />


# 🔌 서비스 포트

## 📍 Core Infrastructure

| Service | Port | Description |
|---|---:|---|
| eureka-server | 19090 | 서비스 디스커버리 서버 |
| api-gateway | 19091 | API Gateway |
| config-server | 19092 | Spring Cloud Config Server |

## 📍 Domain Services

| Service | Port | Description |
|---|---:|---|
| user-service | 19093 | 사용자, 인증, 권한 관리 |
| order-service | 19094 | 주문 관리 |
| hub-service | 19095 | 허브 및 허브 경로 관리 |
| company-service | 19096 | 업체 관리 |
| product-service | 19097 | 상품 관리 |
| stock-service | 19098 | 재고 관리 |
| delivery-service | 19099 | 배송 및 배송 경로 관리 |
| slack-service | 19100 | Slack 알림 처리 |
| ai-service | 19101 | AI 메시지 생성 및 관리 |

## 📍 External Infrastructure

| Service | Port | Description |
|---|---:|---|
| PostgreSQL | 5432 | 관계형 데이터베이스 |
| Redis | 6379 | 캐시, 토큰, 분산락 |
| Zipkin | 9411 | 분산 추적 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 모니터링 대시보드 |

# ⚙️ 기술 스택

## 📍 Backend
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- Spring Cloud OpenFeign
- Resilience4j
- ArchUnit

## 📍 Database / Cache
- PostgreSQL
- Redis

## 📍 Messaging / Event Driven
- Redis Streams
- Apache Kafka

## 📍 MSA
- Spring Cloud Gateway
- Eureka Service Discovery
- Spring Cloud Config Server
- Config Repository

## 📍 Infrastructure
- AWS EC2
- AWS RDS
- AWS ECR
- AWS ALB
- AWS VPC
- Public / Private Subnet
- Internet Gateway

## 📍 DevOps / CI/CD
- Docker
- Docker Compose
- GitHub Actions

## 📍 Monitoring / Tracing
- Prometheus
- Grafana
- Zipkin
- Spring Boot Actuator

## 📍 External API / AI
- Slack API
- Gemini API

---

# 🍿 프로젝트 구조

```text
hub-link
├── core-common                   # 서비스 공통 모듈
│     ├── auth                      # 인증/인가 공통 처리
│     ├── error                     # 공통 예외 및 에러 코드
│     ├── JpaAuditing               # JPA Auditing 공통 설정
│     ├── response                  # 공통 응답 형식
│     └── stream                    # 이벤트 스트림 공통 상수/DTO
│
├── eureka-server                 # 서비스 디스커버리 서버
├── config-server                 # 중앙 설정 관리 서버
│
├── api-gateway                   # API Gateway
│     ├── config                    # Gateway 및 Swagger 설정
│     ├── exception                 # Gateway 예외 처리
│     ├── filter                    # 인증/인가 필터
│     ├── response                  # Gateway 응답 처리
│     └── util                      # Gateway 유틸
│
├── user-service                  # 사용자, 인증, 권한 관리
├── hub-service                   # 허브 및 허브 경로 관리
├── company-service               # 업체 관리
├── product-service               # 상품 관리
├── stock-service                 # 재고 관리
├── order-service                 # 주문 관리
├── delivery-service              # 배송 및 배송 경로 관리
├── slack-service                 # Slack 알림 처리
└── ai-service                    # AI 메시지 생성 및 관리
```

---



# 🧀 브랜치 전략

```text
main
develop
feature/*
hotfix/*
```

---


# 🫡 협업 규칙

- PR 기반 코드 리뷰 진행
- Gemini Code Assist 기반 AI 코드 리뷰 적용
- 코드 및 Git 컨벤션 문서 기반 협업 진행

---
# 📚 API 문서

| 환경     | Swagger URL                                                                          |
| ------ | ------------------------------------------------------------------------------------ |
| Local  | http://localhost:19091/swagger-ui/index.html                                         |
| Deploy | http://hublink-alb-1495218755.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html |
---

# 🔐 환경 변수

프로젝트 루트에 `.env` 파일을 생성하고 아래 값을 환경에 맞게 수정합니다.
- 로컬 버전

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/hublink
DB_USERNAME=
DB_PASSWORD=

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Eureka / Config
EUREKA_URL=http://localhost:19090/eureka/
CONFIG_SERVER_URL=http://localhost:19092

# JWT
JWT_SECRET=change-me-change-me-change-me-change-me

# External API
SLACK_BOT_TOKEN=xoxb-your-slack-bot-token

AI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
AI_API_KEY=your-gemini-api-key


# Monitoring / Tracing
ZIPKIN_URL=http://localhost:9411/api/v2/spans

# ECR
ECR_REGISTRY=your-aws-account-id.dkr.ecr.ap-northeast-2.amazonaws.com


# Swagger
SWAGGER_GATEWAY_URL=http://localhost:19091
```

# 🔥 실행 방법

## 📍 Clone

```bash
git clone https://github.com/organization/hublink.git
```

## 📍 환경 변수 파일 생성
- .env.example 파일을 참고하여 프로젝트 루트에 .env 파일을 생성
- 필요한 값을 로컬 환경에 맞게 수정

## 📍 전체 서비스 빌드
```bash
./gradlew clean build
```

- 테스트 제외하고 빌드
```bash
./gradlew clean build -x test
```

## 📍 Docker Compose 실행
```bash
docker compose up -d
```

- 특정 서비스만 실행
```bash
docker compose up -d api-gateway order-service delivery-service
```

## 📍 실행 상태 확인
```bash
docker compose ps
```

- 서비스 로그 확인
```bash
docker compose logs -f api-gateway
```

## 📍 주요 접속 주소

| 항목                  | URL                                              |
| ------------------- | ------------------------------------------------ |
| API Gateway         | [http://localhost:19091](http://localhost:19091) |
| Eureka Dashboard    | [http://localhost:19090](http://localhost:19090) |
| Zipkin              | [http://localhost:9411](http://localhost:9411)   |
| Prometheus          | [http://localhost:9090](http://localhost:9090)   |
| Grafana             | [http://localhost:3000](http://localhost:3000)   |

## 📍 종료
```bash
docker compose down
```
---

# 💡 확장 기능
- Kafka 기반 이벤트 스트리밍 도입
- Saga 패턴 기반 분산 트랜잭션 처리
- Redis 분산락 기반 동시성 제어
