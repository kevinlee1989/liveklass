# LiveKlass — 크리에이터 정산 API (v2)

강의 플랫폼의 크리에이터 정산을 처리하는 백엔드 API 서버입니다.  
크리에이터/강의/판매/취소 내역을 관리하고, 월별 정산 계산 및 상태 관리 기능을 제공합니다.

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

애플리케이션 시작 시 샘플 데이터(`DataInitializer`)가 자동 삽입됩니다.

- API 서버: `http://localhost:8080`
- DB: `localhost:5432` / DB명: `liveklass` / 계정: `postgres`

### 3. 테스트 실행
```bash
./gradlew test
```

> 테스트도 실제 PostgreSQL에 연결하므로 docker-compose가 먼저 실행 중이어야 합니다.

---

## 전체 API 목록

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
| `GET` | `/sale-records?creatorId=&from=&to=` | 판매 내역 목록 조회 (기간 필터) |

### CancellationRecord (취소 내역)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `POST` | `/cancellation-records` | 취소/환불 내역 등록 |

### Settlement (정산)

| 메서드 | 엔드포인트 | 설명 |
|---|---|---|
| `GET` | `/settlements/creators/{creatorId}?month=` | 크리에이터 월별 정산 조회 |
| `PATCH` | `/settlements/creators/{creatorId}?month=` | 정산 상태 변경 (CONFIRMED / PAID) |
| `GET` | `/settlements/summary?from=&to=` | 운영자용 기간별 전체 정산 집계 |

---

## 도메인 구조

```
com.example
├── creator/          크리에이터 등록 및 목록 조회
├── course/           강의 등록, 제목 수정, 삭제
├── sale/             판매 내역 등록 및 조회
├── cancellation/     취소/환불 내역 등록
├── settlement/       월별 정산 계산 및 상태 관리
├── common/           GlobalExceptionHandler, ErrorResponse
└── config/           DataInitializer (샘플 데이터)
```

---

## 정산 흐름

```
PENDING → CONFIRMED → PAID
```

| 상태 | 설명 |
|---|---|
| `PENDING` | DB 레코드 없음. 요청마다 SaleRecord/CancellationRecord를 실시간 집계 |
| `CONFIRMED` | 운영자가 확정. 현재 시점 금액을 DB에 스냅샷 저장. 이후 원본 데이터 변경과 무관하게 금액 고정 |
| `PAID` | 지급 완료. paidAt 기록. 재지급 방지 |

### 수수료 계산

```
netSales         = totalSales - totalRefunds
platformFee      = netSales × 20%  (소수점 버림)
settlementAmount = netSales - platformFee
```

---

## 샘플 데이터 (DataInitializer)

| 종류 | 내용 |
|---|---|
| Creator | creator-1(김강사), creator-2(이강사), creator-3(박강사) |
| Course | course-1~2(creator-1), course-3(creator-2), course-4(creator-3) |
| SaleRecord | sale-1~7 (2025-01 ~ 2025-03 분포) |
| CancellationRecord | cancel-1~3 (sale-3, sale-4, sale-5 환불) |

---

## v2 신규 추가 내역

### 기능 추가

| 항목 | 내용 |
|---|---|
| `GET /creators` | 전체 크리에이터 목록 조회 (`CreatorService.findAll()`) |
| `PATCH /courses/{courseId}/title` | 강의 제목 수정 (`Course.changeTitle()` 활용) |
| `DELETE /courses/{courseId}` | 강의 삭제. 존재하지 않는 ID 요청 시 400 반환 |

### DTO 추가

| DTO | 위치 | 역할 |
|---|---|---|
| `CourseResponse` | `course/dto/` | 강의 수정 응답 (id, creatorId, title) |

### 버그 수정

| 항목 | 내용 |
|---|---|
| `DELETE /courses/{courseId}` 응답 코드 | 기존 200 → **204 No Content** 로 수정 (REST 표준 준수) |

### 테스트 추가

| 파일 | 테스트 수 | 검증 내용 |
|---|---|---|
| `CreatorApiTest` | 3개 | 전체 목록 조회, 추가 후 조회, 응답 필드 확인 |
| `CourseApiTest` | 6개 | 제목 수정 성공/실패, 빈 제목 검증, 삭제 성공/실패 |

---

## 전체 테스트 목록

| 파일 | 도메인 | 테스트 수 |
|---|---|---|
| `CreatorApiTest` | Creator | 3 |
| `CourseApiTest` | Course | 6 |
| `SaleRecordApiTest` | Sale | 3 |
| `SaleRecordListApiTest` | Sale | 3 |
| `SaleRecordListBoundaryTest` | Sale | 4 |
| `SaleRecordValidationTest` | Sale | 여러 |
| `CancellationRecordApiTest` | Cancellation | 여러 |
| `CancellationRecordValidationTest` | Cancellation | 여러 |
| `CancellationRefundRuleTest` | Cancellation | 여러 |
| `SettlementCalculationApiTest` | Settlement | 여러 |
| `SettlementStatusTest` | Settlement | 11 |
| `SettlementEdgeCaseTest` | Settlement | 여러 |
| `SettlementSummaryApiTest` | Settlement | 여러 |

---

## 문서

| 문서 | 내용 |
|---|---|
| [API 목록 및 예시](docs/API.md) | 전체 엔드포인트, 요청/응답 예시, 에러 케이스 |
| [데이터 모델](docs/DATA_MODEL.md) | 엔티티 구조, 테이블 컬럼, 관계도 |
| [설계 결정](docs/DECISIONS.md) | 요구사항 해석, 가정, 결정 이유 |
| [테스트 실행](docs/TESTING.md) | 테스트 방법 및 시나리오 |
| [미구현 / 제약사항](docs/CONSTRAINTS.md) | 구현 범위 밖의 항목 |
