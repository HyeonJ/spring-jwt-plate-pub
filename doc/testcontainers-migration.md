# H2 → Testcontainers 마이그레이션

## 왜 변경했는가

H2 PostgreSQL 호환 모드의 한계:
- `AUTO_INCREMENT` → `GENERATED ALWAYS AS IDENTITY` 문법 차이
- `ON CONFLICT ... DO UPDATE` 미지원
- `INTERVAL '7 days'` 미지원
- MyBatis DTD URL 문제

**결론:** 운영은 PostgreSQL인데 테스트는 H2로 하면 "테스트 통과, 운영에서 터짐" 발생.

## 변경 내용

### 의존성 변경 (`build.gradle.kts`)

```kotlin
// 삭제
testRuntimeOnly("com.h2database:h2")

// 추가
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:postgresql")
testImplementation("org.testcontainers:junit-jupiter")
```

### 테스트 설정 변경 (`application-test.yml`)

```yaml
# 이전: H2 + schema-test.sql
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  flyway:
    enabled: false
  sql:
    init:
      schema-locations: classpath:schema-test.sql

# 이후: Testcontainers + Flyway
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
# (datasource는 @ServiceConnection이 자동 설정)
```

### 테스트 코드 변경 (`AuthControllerTest.java`)

```java
// 추가된 어노테이션/필드
@Testcontainers
class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    // ... 테스트 메서드는 변경 없음
}
```

`@ServiceConnection`이 datasource url, username, password를 자동으로 주입해줌.
Spring Boot 3.1+ 공식 기능.

### 삭제된 파일

- `src/test/resources/schema-test.sql` — Flyway 마이그레이션이 대신함

### 추가된 파일

- `src/test/resources/docker-java.properties` — Docker API 버전 호환성 설정

## Docker Desktop 호환성 이슈

### 문제

Docker Desktop 4.52+ (Engine v29)부터 최소 API 버전이 **1.44**로 올라감.
Testcontainers 내부의 docker-java 라이브러리가 API **1.32**로 요청하면 **400 BadRequest** 발생.

### 증상

```
EnvironmentAndSystemPropertyClientProviderStrategy: failed with exception 
BadRequestException (Status 400: {"ID":"","Containers":0,...})
```

### 해결

`src/test/resources/docker-java.properties`:
```properties
api.version=1.44
```

### 참고 이슈

- [testcontainers/testcontainers-java#11235](https://github.com/testcontainers/testcontainers-java/issues/11235)
- [testcontainers/testcontainers-java#11422](https://github.com/testcontainers/testcontainers-java/issues/11422)

향후 Testcontainers가 docker-java 버전을 올리면 이 설정 파일은 제거해도 됨.
