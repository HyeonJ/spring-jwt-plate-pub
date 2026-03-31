# spring-jwt-plate

Spring Boot JWT 인증 보일러플레이트 프로젝트.

## 기술 스택
- Java 21, Gradle (Kotlin DSL)
- Spring Boot 3.4, Spring Security 6
- PostgreSQL + MyBatis
- JWT (Access Token 30분 + Refresh Token 7일)
- SpringDoc/Swagger, Lombok, Validation

## 실행 방법
```bash
# 1. PostgreSQL 실행
docker compose up -d

# 2. 앱 실행
./gradlew bootRun

# 3. Swagger UI
# http://localhost:8080/swagger-ui.html
```

## 패키지 구조
```
com.example.app
├── config/       SecurityConfig, CorsConfig, SwaggerConfig
├── security/     JwtProvider, JwtFilter, CustomUserDetails
├── controller/   AuthController
├── service/      AuthService
├── mapper/       MemberMapper (MyBatis)
├── dto/          LoginRequest, SignupRequest, TokenResponse, RefreshRequest
├── exception/    GlobalExceptionHandler, CustomException, ErrorCode
└── common/       ApiResponse
```

## API 엔드포인트
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/signup | 회원가입 | X |
| POST | /api/auth/login | 로그인 | X |
| POST | /api/auth/refresh | 토큰 갱신 | X |
| POST | /api/auth/logout | 로그아웃 | O |

## DB 스키마
- `member` — 회원 테이블 (id, email, password, name, created_at)
- `refresh_token` — 리프레시 토큰 (member_id UNIQUE, token, expires_at)
- DDL: `src/main/resources/schema.sql`

## 개발 규칙
- 글로벌 CLAUDE.md 규칙 따를 것 (코딩 컨벤션, SLF4J 로그 패턴 등)
- MyBatis mapper XML: `src/main/resources/mapper/`
- API 응답: `ApiResponse.ok(data)` / `ApiResponse.error(message)`
- 예외: `ErrorCode` enum에 정의 → `CustomException`으로 throw
