# spring-jwt-plate

Spring Boot JWT 인증 보일러플레이트 프로젝트.

## 기술 스택
- Java 21, Gradle (Kotlin DSL)
- Spring Boot 3.4, Spring Security 6
- PostgreSQL + MyBatis
- JWT (Access Token 30분 + Refresh Token 7일)
- Flyway DB 마이그레이션
- SpringDoc/Swagger, Lombok, Validation

## 실행 방법
```bash
# 1. PostgreSQL 실행
docker compose up -d

# 2. JWT Secret 환경변수 설정 (dev 프로필은 기본값 사용)
export JWT_SECRET=your-base64-encoded-secret

# 3. 앱 실행
./gradlew bootRun

# 4. Swagger UI
# http://localhost:8080/swagger-ui.html

# 5. 테스트
./gradlew test
```

## 패키지 구조
```
com.example.app
├── config/       SecurityConfig, CorsConfig, SwaggerConfig
├── security/     JwtProvider, JwtFilter, CustomUserDetails
├── controller/   AuthController
├── service/      AuthService
├── mapper/       MemberMapper (MyBatis)
├── dto/          LoginRequest, SignupRequest, TokenResponse, RefreshRequest, MemberDto, RefreshTokenDto
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

## DB 마이그레이션
- Flyway 사용: `src/main/resources/db/migration/`
- V1: member + refresh_token 테이블

## 테스트
- H2 인메모리 DB (PostgreSQL 호환 모드)
- `src/test/resources/application-test.yml`
- AuthControllerTest: 회원가입/로그인/토큰갱신/로그아웃 통합 테스트

## 개발 규칙
- 글로벌 CLAUDE.md 규칙 따를 것 (코딩 컨벤션, SLF4J 로그 패턴 등)
- MyBatis mapper는 엔티티/DTO 클래스 사용 (Map 금지)
- JWT Secret 하드코딩 금지 — 환경변수 필수
- Refresh Token 갱신 시 delete → insert (덮어쓰기 의존 금지)
- API 응답: `ApiResponse.ok(data)` / `ApiResponse.error(message)`
- 예외: `ErrorCode` enum에 정의 → `CustomException`으로 throw
