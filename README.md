# spring-jwt-plate

Spring Boot + JWT 인증 보일러플레이트.

## 기술 스택

- Java 21 / Gradle (Wrapper 포함)
- Spring Boot 3.4 / Spring Security 6
- PostgreSQL 15 / MyBatis
- JWT (Access Token 30분 + Refresh Token 7일)
- Flyway DB 마이그레이션
- Testcontainers (테스트용 PostgreSQL)
- SpringDoc Swagger UI

## 사전 준비

- **Java 21** (또는 17 이상)
- **Docker Desktop** 설치 및 실행

## 시작하기

```bash
# 1. PostgreSQL 실행
docker compose up -d

# 2. 앱 실행 (dev 프로필, JWT_SECRET 기본값 사용)
./gradlew bootRun

# 3. 테스트 실행 (Testcontainers가 PostgreSQL 자동 실행)
./gradlew test
```

Swagger UI: http://localhost:8080/swagger-ui.html

## API

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/auth/signup` | 회원가입 | X |
| POST | `/api/auth/login` | 로그인 | X |
| POST | `/api/auth/refresh` | 토큰 갱신 | X |
| POST | `/api/auth/logout` | 로그아웃 | O (Bearer Token) |

## 프로젝트 구조

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

## 테스트

Testcontainers로 실제 PostgreSQL 컨테이너를 띄워서 테스트합니다.
H2 같은 가짜 DB가 아닌 진짜 PostgreSQL이므로 운영 환경과 동일한 조건.

```bash
./gradlew test
```

> Docker Desktop이 실행 중이어야 합니다.

### Docker Desktop + Testcontainers 호환성 참고

Docker Desktop 4.52+ (Engine v29)를 사용하는 경우, Docker API 최소 버전이 1.44로 올라갔습니다.
이 프로젝트는 `src/test/resources/docker-java.properties`에 `api.version=1.44`를 설정하여 호환성을 확보했습니다.

## DB 마이그레이션

Flyway 사용: `src/main/resources/db/migration/`

## 환경 설정

| 환경변수 | 설명 | 기본값 (dev) |
|----------|------|-------------|
| `JWT_SECRET` | JWT 서명 키 (Base64) | 개발용 기본값 사용 |
| `POSTGRES_*` | DB 접속 정보 | docker-compose.yml 참고 |
