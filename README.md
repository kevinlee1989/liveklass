# LiveKlass — 크리에이터 정산 API

강의 플랫폼의 크리에이터 정산을 처리하는 백엔드 API 서버입니다.
판매 내역과 취소 내역을 기반으로 월별 정산 금액을 계산하고, 운영자용 집계 기능을 제공합니다.

---

## 기술 스택

| 레이어 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Build | Gradle |
| Utilities | Lombok |

---

## 실행 방법

### 사전 요구사항
- Docker Desktop
- JDK 17 이상

### 1. PostgreSQL 실행

```bash
docker-compose up -d
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션이 시작되면 샘플 데이터가 자동으로 삽입됩니다.

- API 서버: http://localhost:8080
- DB: `localhost:5432` / DB명: `liveklass` / 계정: `postgres:postgres`

> `spring.jpa.hibernate.ddl-auto=create-drop` 설정으로 앱 시작 시 테이블이 자동 생성됩니다.

---

## 문서

| 문서 | 내용 |
|---|---|
| [API 목록 및 예시](docs/API.md) | 전체 엔드포인트, 요청/응답 예시 |
| [데이터 모델](docs/DATA_MODEL.md) | 엔티티 구조 및 관계 |
| [설계 결정](docs/DECISIONS.md) | 요구사항 해석, 가정, 설계 이유 |
| [테스트 실행](docs/TESTING.md) | 테스트 방법 및 시나리오 |
| [미구현 / 제약사항](docs/CONSTRAINTS.md) | 구현 범위 밖의 항목 |
| [AI 활용 범위](docs/AI_USAGE.md) | AI 도구 사용 내역 |
