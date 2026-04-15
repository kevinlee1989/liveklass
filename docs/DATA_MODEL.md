# 데이터 모델

---

## 엔티티 관계도

```
Creator ──< Course ──< SaleRecord ──< CancellationRecord
```

---

## Creator (크리에이터)

테이블명: `creators`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | VARCHAR | PK, 외부에서 지정 (예: "creator-1") |
| name | VARCHAR | 크리에이터 이름 |

---

## Course (강의)

테이블명: `courses`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | VARCHAR | PK, 외부에서 지정 (예: "course-1") |
| creator_id | VARCHAR | FK → creators.id |
| title | VARCHAR | 강의 제목 |

---

## SaleRecord (판매 내역)

테이블명: `sale_records`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | VARCHAR | PK, 외부에서 지정 (예: "sale-1") |
| course_id | VARCHAR | FK → courses.id |
| student_id | VARCHAR | 수강생 ID |
| amount | NUMERIC(12,2) | 결제 금액 |
| paid_at | TIMESTAMPTZ | 결제 일시 (정산 월 기준 필드) |

---

## CancellationRecord (취소 내역)

테이블명: `cancellation_records`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT | PK, DB 자동 생성 |
| sale_record_id | VARCHAR | FK → sale_records.id |
| refund_amount | NUMERIC(12,2) | 환불 금액 (원결제 금액과 다를 수 있음) |
| canceled_at | TIMESTAMPTZ | 취소 일시 (정산 월 기준 필드) |

---

## Settlement (정산 확정 내역)

테이블명: `settlements`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT | PK, DB 자동 생성 |
| creator_id | VARCHAR | 크리에이터 ID (FK 없음, 논리적 참조) |
| month | VARCHAR | 정산 연월 (예: "2025-03") |
| status | VARCHAR | 정산 상태 (`PENDING` / `CONFIRMED` / `PAID`) |
| total_sales | NUMERIC(12,2) | 확정 시점 총 판매금액 스냅샷 |
| total_refunds | NUMERIC(12,2) | 확정 시점 총 환불금액 스냅샷 |
| net_sales | NUMERIC(12,2) | 확정 시점 순판매금액 스냅샷 |
| platform_fee | NUMERIC(12,2) | 확정 시점 플랫폼 수수료 스냅샷 |
| settlement_amount | NUMERIC(12,2) | 확정 시점 정산 예정 금액 스냅샷 |
| sale_count | INT | 확정 시점 판매 건수 스냅샷 |
| cancellation_count | INT | 확정 시점 취소 건수 스냅샷 |
| confirmed_at | TIMESTAMPTZ | 운영자 확정 일시 |
| paid_at | TIMESTAMPTZ | 지급 완료 일시 (nullable) |

**유니크 제약:** `(creator_id, month)` — 동일 크리에이터·월 중복 정산 방지

> `PENDING` 상태는 DB에 레코드가 없는 상태입니다. GET 요청 시 실시간 계산하여 반환하며, 레코드는 `CONFIRMED` 전환 시점에 처음 생성됩니다.

---

## 핵심 설계 원칙

**판매와 취소의 정산 월은 독립적으로 계산됩니다.**

- `SaleRecord.paidAt` → 판매가 속하는 정산 월 결정
- `CancellationRecord.canceledAt` → 취소가 속하는 정산 월 결정

예시: sale-5는 1월 31일 판매, cancel-3은 2월 3일 취소
- 1월 정산 → sale-5의 판매금액 포함
- 2월 정산 → cancel-3의 환불금액 포함

**크리에이터 접근 경로**

SaleRecord에서 크리에이터를 찾으려면:
```
SaleRecord → Course → Creator
```

CancellationRecord에서 크리에이터를 찾으려면:
```
CancellationRecord → SaleRecord → Course → Creator
```

**정산 상태 흐름**

```
PENDING (레코드 없음, 동적 계산)
   ↓ PATCH { "status": "CONFIRMED" }
CONFIRMED (스냅샷 저장, 금액 고정)
   ↓ PATCH { "status": "PAID" }
PAID (paidAt 기록, 재지급 방지)
```
