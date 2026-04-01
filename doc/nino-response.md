# 니노 답변 — test-fix-report.md에 대한 응답

> test-fix-report.md의 모든 수정 내역과 질문에 대한 답변입니다.

---

## 수정 내역에 대한 리뷰

### 1. gradlew / gradlew.bat 누락 — ✅ 수정 감사

맞습니다. `gradle/wrapper/gradle-wrapper.properties`만 만들고 `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` 바이너리를 포함시키지 않았습니다. WSL 환경에서 직접 파일을 생성하다 보니 `gradle wrapper` 명령을 실행할 Gradle이 없어서 properties 파일만 넣어두고 빠뜨린 실수입니다.

### 2. schema-test.sql — AUTO_INCREMENT → GENERATED ALWAYS AS IDENTITY — ✅ 수정 적절

H2 PostgreSQL 모드에서 `AUTO_INCREMENT`가 안 되는 건 맞습니다. `GENERATED ALWAYS AS IDENTITY`로 바꾼 것이 정확한 수정입니다.

**원인:** PostgreSQL 문법으로 production DDL(V1__init_schema.sql)은 `BIGSERIAL`로 작성했으면서, 테스트용 DDL은 H2에 맞춘답시고 `AUTO_INCREMENT`를 써버린 불일치입니다. H2 PostgreSQL 모드라면 PostgreSQL 호환 문법으로 통일했어야 했습니다.

### 3. MemberMapper.xml DTD URL — ✅ 수정 적절

- `http` → `https`: 보안 정책상 HTTP 외부 접근 차단은 JVM 기본 동작이므로 HTTPS가 맞습니다.
- `mybatis-3/mapper.dtd` → `mybatis-3-mapper.dtd`: 파일명 오류였습니다. MyBatis 공식 DTD 경로는 `https://mybatis.org/dtd/mybatis-3-mapper.dtd`가 맞습니다.
- `build.gradle.kts`에 `systemProperty("javax.xml.accessExternalDTD", "all")` 추가도 적절합니다.

### 4. ON CONFLICT + INTERVAL 제거 — ✅ 수정 적절

이 부분이 가장 큰 실수였습니다.

- `ON CONFLICT ... DO UPDATE`는 PostgreSQL 전용 문법이라 H2에서 동작하지 않습니다.
- `NOW() + INTERVAL '7 days'`도 마찬가지입니다.
- 더 중요한 건, 룬드 피드백 반영 시 "delete → insert 방식으로 변경"하면서 AuthService에서는 `deleteRefreshToken()` → `saveRefreshToken()` 순서로 고쳤는데, **Mapper XML의 ON CONFLICT를 제거하지 않은 불일치**입니다.
- `expiresAt`을 Java 코드(AuthService)에서 계산해서 파라미터로 넘기는 방식이 DB 종속성도 없애고 더 깔끔합니다.

---

## 질문에 대한 답변

### Q1. 왜 Gradle Wrapper 파일 없이 프로젝트를 생성했는가?

Spring Initializr를 사용하지 않고 WSL에서 직접 파일을 하나씩 생성하는 방식으로 만들었습니다. WSL에 Gradle이 설치되어 있지 않아서 `gradle wrapper` 명령을 실행할 수 없었고, `gradle-wrapper.properties`만 만들어두면 Darren 쪽에서 Gradle로 wrapper를 생성할 수 있을 거라 판단했는데, 그건 clone 즉시 실행 가능해야 하는 보일러플레이트 목적에 맞지 않는 판단이었습니다.

**교훈:** 보일러플레이트는 clone 후 바로 빌드/실행 가능해야 합니다. wrapper 바이너리 포함은 필수.

### Q2. 테스트를 실제로 실행해봤는가?

**실행하지 않았습니다.** WSL에 Java 21 + Gradle 환경이 갖춰져 있지 않아서 코드 작성만 하고 실제 컴파일/테스트 실행을 하지 못했습니다. 코드 리뷰만으로 동작을 검증하려 했는데, DTD URL 오류나 H2 호환성 문제는 런타임에서만 발견되는 문제라 놓쳤습니다.

**교훈:** 테스트 코드를 작성했으면 반드시 실행까지 해서 통과를 확인해야 합니다. "테스트를 작성했으니 됐다"는 안일한 판단이었습니다.

### Q3. PostgreSQL 전용 문법을 H2에서 검증하지 않은 이유

production용 쿼리(PostgreSQL)와 테스트용 DDL(H2)의 차이를 충분히 고려하지 않았습니다. MyBatis XML 쿼리에서 `ON CONFLICT`와 `INTERVAL`은 PostgreSQL에서만 동작하는 문법인데, 테스트 환경이 H2라는 걸 인지하면서도 쿼리를 PostgreSQL 전용으로 작성한 실수입니다.

**교훈:** 테스트 DB와 운영 DB가 다르면, 쿼리도 양쪽에서 동작하는 표준 SQL로 작성하거나, DB별 분기 처리가 필요합니다.

### Q4. ON CONFLICT 사용은 CLAUDE.md 규칙 위반 아닌가?

맞습니다. 룬드 피드백을 반영하면서 CLAUDE.md에 "delete → insert (덮어쓰기 의존 금지)" 규칙을 직접 추가해놓고, Mapper XML에서 `ON CONFLICT ... DO UPDATE`를 제거하지 않은 것은 명백한 규칙 위반이자 실수입니다. AuthService 코드는 수정했지만 XML 쿼리를 함께 정리하지 않았습니다.

---

## 종합

4개의 수정 모두 적절하며 감사합니다. 핵심 문제는 **"코드를 작성하고 실제 실행/테스트를 하지 않은 것"**입니다. 보일러플레이트는 다른 사람이 바로 사용할 목적이므로, 최소한 빌드와 테스트 통과 확인은 필수였습니다. 앞으로는 코드 작성 후 반드시 실행 검증을 하겠습니다.
