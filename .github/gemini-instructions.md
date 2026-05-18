# AI Code Review Guidelines

## Project Context
- This project is a Spring Boot 3.x based MSA logistics platform.
- Services communicate through API Gateway and service-to-service calls.
- Authentication uses JWT.
- Database is PostgreSQL.
- Messaging may use RabbitMQ first, with possible Kafka expansion later.

## Review Priorities
Please focus on:
1. Security issues
2. Transaction boundary problems
3. Validation omissions
4. Exception handling consistency
5. JPA N+1 or lazy loading risks
6. API response consistency
7. MSA service boundary violations
8. Naming and package convention consistency

## Backend Convention
- Controller should only handle request/response mapping.
- Business logic should be placed in Service.
- Repository should only handle persistence queries.
- DTO validation should use Bean Validation annotations.
- Entity should not be directly exposed in API responses.
- Use UUID for main domain identifiers unless otherwise specified.

## Transaction Convention
- Use `@Transactional` on service-layer write operations.
- Use `@Transactional(readOnly = true)` on read operations.
- Avoid external API calls inside long transactions.
- Check consistency between Order, Delivery, Payment, and Inventory flows.

## Error Handling
- Use global exception handling.
- Do not return raw exception messages to clients.
- Error responses should follow the common response format.

## API Response Format
Success response:
{
"status": 200,
"message": "SUCCESS",
"data": {}
}

Error response:
{
"status": 400,
"message": "ERROR_CODE",
"errors": []
}

## MSA Rules
- Gateway performs first-level JWT validation.
- Each service must re-check authorization when needed.
- Do not directly access another service's database.
- Prefer API call or messaging for cross-service communication.
- Avoid circular service dependencies.

## Messaging Policy
- RabbitMQ is the initial message broker.
- Kafka may be considered later for event streaming expansion.
- Events should include enough identifiers for tracing.
- Consumers should be idempotent where possible.

## Review Style
- Prioritize important issues over minor style comments.
- Explain why the issue matters.
- Suggest concrete code-level improvements.
- Avoid excessive comments on trivial formatting.

## Language
- Write review comments in Korean.
- Keep explanations concise and practical.
- Avoid overly formal expressions.

## Additional References
Please also follow the conventions described in:
- docs/code-convention.md
- docs/git-convention.md