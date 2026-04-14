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
