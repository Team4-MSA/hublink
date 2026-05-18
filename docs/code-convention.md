# Code Convention

## Java 스타일

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)를 따른다.

## REST API 규칙

### URI 규칙

- URI는 소문자와 하이픈(`-`)만 사용한다.
- 대문자, 언더스코어(`_`)는 사용하지 않는다.

예시:

```text
/api/v1/hub-routes
/api/v1/order-items
```

### 자원 이름 규칙

- URI에는 동사를 사용하지 않고 명사를 사용한다.
- 자원명은 복수형을 사용한다.

예시:

```text
/users
/orders
/hub-routes
```

## 공통 응답/예외 처리

MSA의 각 서비스가 동일한 JSON 구조의 성공/실패 응답을 반환하도록 `CommonResponse` 및 `@RestControllerAdvice`를 글로벌로 구성한다.

### 성공 응답 예시

```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {}
}
```

### 실패 응답 예시

```json
{
  "status": 400,
  "message": "ERROR_CODE",
  "errors": []
}
```


### 예외 처리 규칙

- 공통 예외 처리는 `@RestControllerAdvice`에서 처리한다.
- 모든 API 응답은 `CommonResponse` 형식을 따른다.
- 클라이언트에게 내부 예외 메시지를 그대로 반환하지 않는다.
- 서비스별 응답 구조가 달라지지 않도록 공통 응답 형식을 유지한다.

## 코드 리뷰 전제
- dependency/plugin 버전 존재 여부는 학습된 지식으로 판단하지 않는다.
- Spring Boot, Spring Cloud, Gradle Plugin 등 버전 관련 지적은 공식 문서 또는 Maven Central 기준으로 검증된 경우에만 작성한다.
- 검증하지 못한 버전 관련 내용은 High/Blocker로 표시하지 않는다.

### 현재 프로젝트 기준 버전
- Java 17
- Spring Boot 3.5.14
- Spring Cloud 2025.0.2

### Spring 버전 호환성 기준
- Spring Cloud 2025.0.x는 Spring Boot 3.5.x 라인과 호환되는 release train이다.
- 따라서 Spring Boot 3.5.x 사용 시 Spring Cloud 2024.0.x로 다운그레이드하라는 리뷰는 작성하지 않는다.