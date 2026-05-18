# 🚛 HubLink 

> 🌱 MSA 기반 물류 · 배송 관리 플랫폼

HubLink는 주문, 배송, 허브, 결제, 사용자 관리를 분리된 서비스로 구성한 MSA 기반 물류 플랫폼입니다.  
대규모 트래픽 환경을 고려하여 서비스 간 결합도를 낮추고, 이벤트 기반 확장 구조를 고려해 설계하였습니다.

주문 생성부터 배송 상태 추적, 허브 이동, 결제 처리, 알림 전송까지의 흐름을 독립적인 서비스 단위로 분리하여 관리합니다.

또한 RabbitMQ 기반 메시징 구조와 향후 Kafka 확장을 고려한 이벤트 기반 아키텍처를 통해 확장성과 유지보수성을 높였습니다.

---

# 🐥 주요 기능

## 📍 주문 관리
- 주문 생성
- 주문 조회
- 주문 상태 관리
- 주문 취소

## 📍 배송 관리
- 배송 생성
- 배송 상태 변경
- 배송 이력 관리
- 허브 이동 추적

## 📍 허브 관리
- 허브 등록 및 조회
- 허브 간 이동 경로 관리

## 📍 결제 관리
- 결제 요청
- 결제 상태 관리
- 결제 내역 조회

## 📍 인증 / 사용자 관리
- JWT 기반 인증
- 사용자 권한 관리
- Gateway 기반 인증 처리

## 📍 알림 기능
- Slack 알림 전송
- 이벤트 기반 알림 처리

---

# 🍟 시스템 아키텍처

<img width="4791" height="3183" alt="인프라설계도" src="https://github.com/user-attachments/assets/e5108c42-c0de-49ec-ae5f-492913c0bc1b" />


---

# 🍒 ERD
<img width="2817" height="1902" alt="HubLink" src="https://github.com/user-attachments/assets/97070363-6f9e-441d-986a-bb536f6473fc" />

---

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

## 📍 Database / Cache
- PostgreSQL
- Redis

## 📍 MSA
- Spring Cloud Gateway
- Eureka Service Discovery
- Config Server
- RabbitMQ
- Kafka(예정)

## 📍 AWS / Infrastructure
- AWS Cloud
- VPC
- Public Subnet / Private Subnet
- Route 53
- Internet Gateway
- Application Load Balancer
- EC2
- RDS
- Amazon ECR

## 📍 DevOps
- Docker
- Docker Compose
- GitHub Actions

## 📍 Monitoring / Tracing
- Prometheus
- Grafana
- Zipkin
- Spring Boot Actuator

## 📍 External API
- Slack API
- Gemini API

---

# 🍿 프로젝트 구조

```text
hub-link
├── common
│       ├── response
│       ├── exception
│       ├── security
│       ├── dto
│       └── util
│
├── eureka-server
│
├── config-server
│
├── api-gateway
│       ├── config
│       ├── filter
│       ├── security
│       └── global
│
├── user-service
├── hub-service
├── company-service
├── product-service
├── stock-service
├── order-service
├── delivery-service
├── slack-service
└── ai-service
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

# 🔥 실행 방법

## 📍 Clone

```bash
git clone https://github.com/organization/hublink.git
```

## 📍 Build

```bash
./gradlew build
```

## 📍 Run

```bash
docker-compose up -d
```

---

# 💡 확장
- Kafka 기반 이벤트 스트리밍 도입
- Saga 패턴 기반 분산 트랜잭션 처리
- Redis 분산락 기반 동시성 제어
