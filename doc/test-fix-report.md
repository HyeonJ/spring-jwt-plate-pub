# 테스트 수정 리포트

보일러플레이트 생성 후 `./gradlew test` 실행 시 **7개 테스트 전부 실패**.
아래는 발견된 문제들과 수정 내역.

---

## 1. gradlew / gradlew.bat 누락

프로젝트에 `gradle/wrapper/` 디렉토리만 있고 `gradlew`, `gradlew.bat` 파일이 없었음.
`gradle-wrapper.jar`도 누락.

**조치:** gradlew, gradlew.bat 스크립트 생성 + gradle-wrapper.jar 다운로드

---

## 2. schema-test.sql — H2에서 AUTO_INCREMENT 문법 에러

**에러:**
```
org.h2.jdbc.JdbcSQLSyntaxErrorException: Syntax error in SQL statement
"CREATE TABLE member ( id BIGINT AUTO_INCREMENT PRIMARY KEY, ... )"
expected "ARRAY, INVISIBLE, VISIBLE, NOT NULL, DEFAULT, GENERATED, ..."
```

**원인:** H2가 PostgreSQL 모드(`MODE=PostgreSQL`)로 동작할 때 `AUTO_INCREMENT`를 지원하지 않음. PostgreSQL 모드에서는 `GENERATED ALWAYS AS IDENTITY`를 사용해야 함.

**이전 코드:** `src/test/resources/schema-test.sql`
```sql
CREATE TABLE member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ...
);

CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ...
);
```

**수정 코드:**
```sql
CREATE TABLE member (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ...
);

CREATE TABLE refresh_token (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ...
);
```

---

## 3. MemberMapper.xml — MyBatis DTD URL 접근 실패

**에러:**
```
org.xml.sax.SAXParseException: accessExternalDTD 속성에서 설정한 제한으로 인해
'http' 액세스가 허용되지 않으므로 외부 DTD 'mapper.dtd' 읽기를 허용하지 않았습니다.
```
→ DTD 접근 허용 후에도:
```
java.io.FileNotFoundException (404)
```

**원인:** DTD URL이 `http://mybatis.org/dtd/mybatis-3/mapper.dtd`로 되어 있었는데, (1) JVM 기본 보안 정책이 외부 DTD HTTP 접근을 차단하고, (2) 해당 URL 자체가 404 (파일명이 `mybatis-3-mapper.dtd`가 맞음).

**이전 코드:** `src/main/resources/mapper/MemberMapper.xml`
```xml
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3/mapper.dtd">
```

**수정 코드:**
```xml
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
```

**추가 조치:** `build.gradle.kts`에 테스트 시 외부 DTD 접근 허용 설정 추가
```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("javax.xml.accessExternalDTD", "all")
}
```

---

## 4. MemberMapper.xml — ON CONFLICT + INTERVAL 문법 (PostgreSQL 전용)

**에러:**
```
Status expected:<200> but was:<500>
```
(로그인 API 호출 시 saveRefreshToken에서 500 에러)

**원인:** `saveRefreshToken` 쿼리에 PostgreSQL 전용 문법 2개가 사용됨:
- `ON CONFLICT (member_id) DO UPDATE SET ...` — H2에서 미지원
- `NOW() + INTERVAL '7 days'` — H2 PostgreSQL 모드에서 미지원

서비스 코드(`AuthService`)에서 이미 `deleteRefreshToken()` → `saveRefreshToken()` 순서로 호출하므로 `ON CONFLICT`는 불필요했음.

**이전 코드:** `src/main/resources/mapper/MemberMapper.xml`
```xml
<insert id="saveRefreshToken">
    INSERT INTO refresh_token (member_id, token, expires_at)
    VALUES (#{memberId}, #{refreshToken}, NOW() + INTERVAL '7 days')
    ON CONFLICT (member_id)
        DO UPDATE SET token      = EXCLUDED.token,
                      expires_at = EXCLUDED.expires_at
</insert>
```

**수정 코드:**
```xml
<insert id="saveRefreshToken">
    INSERT INTO refresh_token (member_id, token, expires_at)
    VALUES (#{memberId}, #{refreshToken}, #{expiresAt})
</insert>
```

**연쇄 수정 — MemberMapper.java:**
```java
// 이전
void saveRefreshToken(@Param("memberId") Long memberId, @Param("refreshToken") String refreshToken);

// 수정
void saveRefreshToken(@Param("memberId") Long memberId, @Param("refreshToken") String refreshToken, @Param("expiresAt") java.time.LocalDateTime expiresAt);
```

**연쇄 수정 — AuthService.java (login, refresh 메서드 둘 다):**
```java
// 이전
memberMapper.saveRefreshToken(member.getId(), refreshToken);

// 수정
LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiration() / 1000);
memberMapper.saveRefreshToken(member.getId(), refreshToken, expiresAt);
```

---

## 질문: 왜 이렇게 오류가 많았는가?

이 보일러플레이트를 생성한 클로드 코드 세션에 다음을 확인해야 함:

1. **gradlew / gradle-wrapper.jar 누락** — 왜 Gradle Wrapper 파일 없이 프로젝트를 생성했는가? Spring Initializr로 생성했다면 자동 포함됐을 텐데.

2. **테스트를 실제로 실행해봤는가?** — `schema-test.sql`의 AUTO_INCREMENT, `MemberMapper.xml`의 ON CONFLICT/INTERVAL, DTD URL 오류 등은 한 번이라도 `./gradlew test`를 돌렸으면 바로 발견됐을 문제들.

3. **PostgreSQL 전용 문법을 테스트 환경(H2)에서 검증하지 않은 이유** — 테스트는 H2를 쓰도록 설정해놓고, 쿼리는 PostgreSQL 전용 문법으로 작성한 것은 명백한 불일치. `ON CONFLICT`와 `INTERVAL`은 H2에서 동작하지 않음.

4. **ON CONFLICT 사용 자체가 CLAUDE.md 규칙 위반** — CLAUDE.md에 "Refresh Token 갱신 시 delete → insert (덮어쓰기 의존 금지)"라고 명시되어 있는데, `ON CONFLICT ... DO UPDATE`를 사용함.
