# LiveKlass — 크리에이터 정산 API 서버 최종 보고서

> 본 문서는 LiveKlass 크리에이터 정산 백엔드 API 서버의 구현 내용을 정리한 최종 보고서입니다.

---

## 1. 프로젝트 개요

강의 플랫폼 **LiveKlass**의 크리에이터 수익 정산을 처리하는 RESTful API 서버입니다.

크리에이터가 개설한 강의에 대한 판매·취소 내역을 관리하고, 이를 기반으로 월별 정산 금액을 자동 산출합니다.  
정산은 **PENDING → CONFIRMED → PAID** 3단계 상태 흐름을 통해 운영자가 순차적으로 확정 및 지급 처리할 수 있도록 설계되었습니다.

운영자는 별도의 집계 API(`GET /settlements/summary`)를 통해 특정 기간 내 전체 크리에이터의 정산 현황을 한눈에 파악할 수 있습니다.

---

## 2. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 26 |
| Framework | Spring Boot 4.0.5 |
| ORM | MyBatis 4.0.1 |
| Database | PostgreSQL 16 |
| DB 마이그레이션 | Flyway 11 |
| Build | Gradle |
| 유틸리티 | Lombok |

---

## 3. 실행 방법

### 사전 조건

로컬 환경에 PostgreSQL이 설치되어 있어야 합니다


### 애플리케이션 실행

```bash
./gradlew bootRun
```

서버 기동 시 Flyway가 자동으로 스키마를 생성하고, `DataInitializer`가 아래 샘플 데이터를 자동 삽입합니다.  
별도의 수동 데이터 세팅 없이 즉시 API를 호출할 수 있습니다.

**자동 삽입 샘플 데이터**

| 구분 | 내용 |
|---|---|
| Creator | creator-1 (김강사), creator-2 (이강사), creator-3 (박강사) |
| Course | course-1~2 (creator-1), course-3 (creator-2), course-4 (creator-3) |
| SaleRecord | sale-1~7 (2025년 1월~3월 분포) |
| CancellationRecord | cancel-1 (sale-3 전액환불), cancel-2 (sale-4 부분환불), cancel-3 (sale-5 전액환불) |

- API 서버: `http://localhost:8080`

---

## 4. 테스트 실행

```bash
./gradlew test
```

총 **13개 테스트 클래스, 72개 테스트 케이스**로 구성되어 있습니다.

각 테스트는 실행 전 DB를 초기화하고 샘플 데이터를 재삽입하여 독립적인 환경에서 수행됩니다.

| 테스트 파일 | 도메인 | 테스트 수 | 검증 내용 |
|---|---|:---:|---|
| `CreatorApiTest` | Creator | 3 | 목록 조회, 추가 후 조회, 응답 필드 확인 |
| `CourseApiTest` | Course | 6 | 제목 수정 성공/실패, 빈 제목 검증, 삭제 성공/실패 |
| `SaleRecordApiTest` | Sale | 3 | 등록 성공, 존재하지 않는 강의 ID, 중복 ID 검증 |
| `SaleRecordListApiTest` | Sale | 5 | 전체 조회, 기간 필터, 기간 외 제외, 응답 필드, 빈 결과 |
| `SaleRecordListBoundaryTest` | Sale | 7 | 시작일·종료일 경계값, from/to 단독 입력 400 처리 |
| `SaleRecordValidationTest` | Sale | 8 | amount/studentId/courseId/paidAt 입력값 검증 |
| `CancellationRecordApiTest` | Cancellation | 3 | 등록 성공, 부분 환불, 존재하지 않는 판매 ID |
| `CancellationRecordValidationTest` | Cancellation | 3 | refundAmount/canceledAt/saleRecordId 입력값 검증 |
| `CancellationRefundRuleTest` | Cancellation | 4 | 초과 환불 거절, 부분 취소 누적, 전액환불 후 추가 취소 |
| `SettlementCalculationApiTest` | Settlement | 6 | 과제 시나리오 1~4, 수수료 계산 정확성, 월 경계 |
| `SettlementStatusTest` | Settlement | 10 | PENDING/CONFIRMED/PAID 전이, 중복 확정·재지급 방지 |
| `SettlementEdgeCaseTest` | Settlement | 6 | 순판매 0원, 소수점 버림, 음수 netSales, 다건 부분환불 합산 |
| `SettlementSummaryApiTest` | Settlement | 8 | 기간별 집계, 크리에이터 단위 합산, 음수 포함 여부 |

---

## 5. 전체 API 목록

### Creator (크리에이터)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `POST` | `/creators` | 크리에이터 등록 |
| `GET` | `/creators` | 전체 크리에이터 목록 조회 |

### Course (강의)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `POST` | `/courses` | 강의 등록 |
| `PATCH` | `/courses/{courseId}/title` | 강의 제목 수정 |
| `DELETE` | `/courses/{courseId}` | 강의 삭제 |

### SaleRecord (판매 내역)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `POST` | `/sale-records` | 판매 내역 등록 |
| `GET` | `/sale-records?creatorId=&from=&to=` | 판매 내역 조회 (기간 필터 선택) |

### CancellationRecord (취소/환불 내역)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `POST` | `/cancellation-records` | 취소/환불 내역 등록 |

### Settlement (정산)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `GET` | `/settlements/creators/{creatorId}?month=YYYY-MM` | 크리에이터 월별 정산 조회 |
| `PATCH` | `/settlements/creators/{creatorId}?month=YYYY-MM` | 정산 상태 변경 (CONFIRMED / PAID) |
| `GET` | `/settlements/summary?from=YYYY-MM-DD&to=YYYY-MM-DD` | 운영자용 기간별 전체 정산 집계 |

---

## 6. 도메인 구조

```
com.example
├── creator/          크리에이터 등록 및 목록 조회
├── course/           강의 등록, 제목 수정, 삭제
├── sale/             판매 내역 등록 및 조회
├── cancellation/     취소/환불 내역 등록 및 누적 환불 검증
├── settlement/       월별 정산 계산, 상태 관리, 운영자 집계
├── common/           GlobalExceptionHandler, ErrorResponse
└── config/           DataInitializer (샘플 데이터 자동 삽입)
```

**엔티티 간 참조 관계**

```
Creator (1)
  └── Course (N)          creator_id → creators.id  ON DELETE CASCADE
        └── SaleRecord (N)    course_id  → courses.id   ON DELETE RESTRICT
              └── CancellationRecord (N)  sale_record_id → sale_records.id  ON DELETE RESTRICT

Creator (1)
  └── Settlement (N)      creator_id → creators.id  ON DELETE CASCADE
```

---

## 7. 정산 흐름 및 핵심 로직

### 7-1. 상태 전이

```
PENDING  →  CONFIRMED  →  PAID
```

| 상태 | 설명 |
|---|---|
| `PENDING` | 정산 레코드 없음. `GET` 요청 시 SaleRecord·CancellationRecord를 실시간 집계하여 동적 반환 |
| `CONFIRMED` | 운영자가 확정 처리. 현재 시점 금액을 DB에 스냅샷으로 저장. 이후 원본 데이터 변경과 무관하게 금액 고정 |
| `PAID` | 지급 완료. `paidAt` 기록. 재지급 방지를 위해 PAID 상태에서 추가 전이 불가 |

**전이 규칙 요약**

- `PENDING` → `CONFIRMED` : 정상 전이
- `CONFIRMED` → `PAID` : 정상 전이
- `PENDING` → `PAID` : **불가** (확정 없이 지급 불가)
- `CONFIRMED` 재요청 : **불가** (이미 확정됨)
- `PAID` 후 모든 전이 : **불가** (지급 완료 상태에서 변경 차단)

### 7-2. 수수료 계산

```
netSales         = totalSales - totalRefunds
platformFee      = netSales × 20%  (소수점 이하 버림, RoundingMode.DOWN)
settlementAmount = netSales - platformFee
```

- **부분 환불** : 환불액만큼만 `totalRefunds`에 반영되어 잔여 판매금액이 정산에 기여합니다.
- **음수 netSales** : 환불액이 판매액을 초과하는 월의 경우 `netSales`가 음수가 되며, 이 경우 `platformFee`도 음수로 계산되어 `settlementAmount`는 `netSales`보다 작아집니다.
- **소수점 처리** : 플랫폼 수수료는 항상 소수점 이하를 버림(DOWN) 처리합니다. (예: 999원 × 20% = 199.8원 → 199원)

*작성일: 2026-06-10*

## 문서

| 문서 | 내용 |
|---|---|
| [API 목록 및 예시](docs/API.md) | 전체 엔드포인트, 요청/응답 예시, 에러 케이스 |
| [데이터 모델](docs/DATA_MODEL.md) | 엔티티 구조, 테이블 컬럼, 관계도 |
| [테스트 실행](docs/TESTING.md) | 테스트 방법, 시나리오 54개 |