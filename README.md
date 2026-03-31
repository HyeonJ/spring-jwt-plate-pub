# spring-jwt-plate

Spring Boot + JWT 인증 보일러플레이트.

## 기술 스택

- Java 21 / Gradle
- Spring Boot 3.4 / Spring Security 6
- PostgreSQL / MyBatis
- JWT (Access + Refresh Token)
- SpringDoc Swagger UI

## 시작하기

```bash
# PostgreSQL 실행
docker compose up -d

# 앱 실행
./gradlew bootRun
```

Swagger UI: http://localhost:8080/swagger-ui.html

## API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |
