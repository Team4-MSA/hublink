# Service Port Convention

## 서비스 포트 규칙

HubLink 프로젝트의 각 서비스는 로컬 개발 환경에서 포트 충돌을 방지하기 위해 고정 포트를 사용한다.

---

## Core Infrastructure

| Service |  Port | Description |
| --- |------:| --- |
| eureka-server | 19090 | 서비스 디스커버리 서버 |
| api-gateway | 19091 | API Gateway |
| config-server | 19092 | Spring Cloud Config Server |

---

## Domain Services

| Service |  Port | Description |
| --- |------:| --- |
| user-service | 19093 | 사용자, 인증, 권한 관리 |
| order-service | 19094 | 주문 관리 |
| hub-service | 19095 | 허브 및 허브 경로 관리 |
| company-service | 19096 | 업체 관리 |
| product-service | 19097 | 상품 관리 |
| stock-service | 19098 | 재고 관리 |
| delivery-service | 19099 | 배송 및 배송 경로 관리 |
| slack-service | 19100 | Slack 알림 처리 |
| ai-service | 19101 | AI 메시지 생성 및 관리 |

---

## External Infrastructure

| Service | Port | Description |
| --- | ---: | --- |
| PostgreSQL | 5432 | 관계형 데이터베이스 |
| Redis | 6379 | 캐시, 토큰, 분산락 |
| RabbitMQ | 5672 | 메시지 브로커 |
| RabbitMQ Management | 15672 | RabbitMQ 관리 콘솔 |
| Zipkin | 9411 | 분산 추적 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 모니터링 대시보드 |

---

## 포트 사용 규칙

- 서비스 포트는 임의로 변경하지 않는다.
- 포트를 변경해야 할 경우 팀원과 공유 후 문서를 함께 수정한다.
- `application.yml` 또는 Config Repo의 서비스별 설정과 이 문서의 포트가 일치해야 한다.
- Gateway 라우팅 설정과 Eureka 등록 정보도 포트 변경 시 함께 확인한다.