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

> `spring.jpa.hibernate.ddl-auto=create-drop` 설정으로 앱 재시작 시 테이블이 자동 생성됩니다.

### 3. 테스트 실행

```bash
./gradlew test
```

> 테스트도 실제 PostgreSQL에 연결하므로 docker-compose가 먼저 실행 중이어야 합니다.

---

## 구현 범위

### 필수 구현

| 기능 | 엔드포인트 |
|---|---|
| 크리에이터 등록 | `POST /creators` |
| 강의 등록 | `POST /courses` |
| 판매 내역 등록 | `POST /sale-records` |
| 취소 내역 등록 | `POST /cancellation-records` |
| 판매 내역 목록 조회 (기간 필터) | `GET /sale-records?creatorId=&from=&to=` |
| 크리에이터별 월별 정산 조회 | `GET /settlements/creators/{creatorId}?month=` |
| 운영자 기간별 집계 | `GET /settlements/summary?from=&to=` |

### 선택 구현

| 기능 | 방식 |
|---|---|
| 정산 확정 상태 관리 (PENDING → CONFIRMED → PAID) | `PATCH /settlements/creators/{creatorId}?month=` |
| 동일 기간 중복 정산 방지 | `(creatorId, month)` DB 유니크 제약 + 서비스 레이어 중복 검증 |
| 수수료율 변경 가능성 설계 반영 | `SettlementService.FEE_RATE` 상수로 분리 (추후 DB 관리 방식으로 교체 용이) |

---

## 추가 검증 케이스

과제 샘플 데이터 외에 직접 설계하고 검증한 케이스 목록입니다.

### 기간 필터 경계값 (`SaleRecordListBoundaryTest`)

| 케이스 | 이유 |
|---|---|
| 시작일 당일 paidAt → 포함 | `from`일 00:00:00 KST 이상이므로 포함되어야 함. 구현 실수가 잦은 경계 |
| 종료일 당일 paidAt → 포함 | `to+1`일 00:00:00 미만 방식이므로 당일이 포함되는지 확인 |
| from만 입력 → 400 | from/to는 반드시 함께 입력해야 함. 한쪽만 입력 시 의미 없는 쿼리가 됨 |
| from > to → 400 | 시작일이 종료일보다 늦으면 결과가 항상 비어 있어 명시적으로 거절 |

### 정산 수수료 소수점 처리 (`SettlementEdgeCaseTest`)

| 케이스 | 이유 |
|---|---|
| 999원 × 20% = 199.8 → 199원 | `RoundingMode.HALF_UP`이면 200원, `DOWN`이면 199원. 버림 여부를 명시적으로 검증 |
| 1001원 × 20% = 200.2 → 200원 | 소수점 올림 방지 추가 검증 |

### 순판매금액 0 / 음수 (`SettlementEdgeCaseTest`)

| 케이스 | 이유 |
|---|---|
| 판매 = 전액환불 → netSales=0, fee=0 | 수수료가 0으로 계산되는지, settlementAmount가 음수가 되지 않는지 확인 |
| 취소만 있는 달 → netSales=-60,000 | 음수 netSales 시 수수료도 음수가 되어 settlementAmount에 환원되는 동작 확인 |

### 여러 course 합산 (`SettlementSummaryApiTest`)

| 케이스 | 이유 |
|---|---|
| creator-1이 course-1, course-2 두 강의 보유 | 집계 시 creatorId 기준으로 합산되는지, course별로 분리되지 않는지 확인 (구현 실수 시 항목이 2개로 쪼개짐) |

### 누적 환불 초과 방지 (`CancellationRefundRuleTest`)

| 케이스 | 이유 |
|---|---|
| 부분 환불 여러 번 → 누적 합계 검증 | 개별 환불은 통과해도 합산이 원결제를 초과하면 거절 |
| 전액 환불 후 1원 추가 시도 | 이미 소진된 환불 한도에 추가 요청 시 거절 확인 |

### 정산 상태 전이 방지

| 케이스 | 이유 |
|---|---|
| 이미 CONFIRMED → CONFIRMED 요청 | 중복 확정 방지 |
| CONFIRMED 없이 PAID 요청 | 순서 보장 — 반드시 확정 후 지급 |
| PAID 후 재전환 시도 | 재지급 방지 |

---

## 문서

| 문서 | 내용 |
|---|---|
| [API 목록 및 예시](docs/API.md) | 전체 엔드포인트, 요청/응답 예시, 에러 케이스 |
| [데이터 모델](docs/DATA_MODEL.md) | 엔티티 구조, 테이블 컬럼, 관계도 |
| [설계 결정](docs/DECISIONS.md) | 요구사항 해석, 가정, 결정 이유 14가지 |
| [테스트 실행](docs/TESTING.md) | 테스트 방법, 시나리오 54개 |
| [미구현 / 제약사항](docs/CONSTRAINTS.md) | 구현 범위 밖의 항목 |
| [AI 활용 범위](docs/AI_USAGE.md) | AI 도구 사용 내역 |
