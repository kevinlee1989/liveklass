# API 목록 및 예시

Base URL: `http://localhost:8080`

---

## 공통 에러 응답

모든 에러는 아래 형식으로 반환됩니다.

```json
{
  "status": 400,
  "message": "에러 메시지"
}
```

| HTTP 상태 | 발생 조건 |
|---|---|
| `400 Bad Request` | 필수 필드 누락, 형식 오류, 비즈니스 규칙 위반 (존재하지 않는 ID, 중복 ID, 환불 초과 등) |
| `500 Internal Server Error` | 서버 내부 오류 |

에러 처리는 `GlobalExceptionHandler` (`@RestControllerAdvice`)에서 일괄 처리합니다.

| 예외 타입 | 응답 코드 | 예시 |
|---|---|---|
| `IllegalArgumentException` | 400 | 존재하지 않는 ID, 중복 ID, 환불 초과, 날짜 파라미터 오류 |
| `MethodArgumentNotValidException` | 400 | `@Valid` 검증 실패 (필수 필드 누락, 음수 금액 등) |
| `HttpMessageNotReadableException` | 400 | JSON 파싱 실패 (OffsetDateTime 형식 오류 등) |
| 그 외 `Exception` | 500 | 예상치 못한 서버 오류 |

---

## 1. 크리에이터 등록

```
POST /creators
```

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | String | Y | 크리에이터 ID |
| name | String | Y | 크리에이터 이름 |

**Request 예시**
```json
{
  "id": "creator-10",
  "name": "최강사"
}
```

**Response** `201 Created`
```json
{ "id": "creator-10" }
```

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| id가 이미 존재함 | `"이미 존재하는 크리에이터 ID입니다: creator-1"` |
| 필수 필드 누락 (`@NotBlank`) | 해당 필드의 검증 메시지 |

---

## 2. 강의 등록

```
POST /courses
```

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | String | Y | 강의 ID |
| creatorId | String | Y | 크리에이터 ID |
| title | String | Y | 강의 제목 |

**Request 예시**
```json
{
  "id": "course-10",
  "creatorId": "creator-1",
  "title": "Redis 입문"
}
```

**Response** `201 Created`
```json
{ "id": "course-10" }
```

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| id가 이미 존재함 | `"이미 존재하는 강의 ID입니다: course-1"` |
| creatorId가 존재하지 않음 | `"존재하지 않는 크리에이터입니다: creator-999"` |
| 필수 필드 누락 (`@NotBlank`) | 해당 필드의 검증 메시지 |

---

## 3. 판매 내역 등록

```
POST /sale-records
```

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | String | Y | 판매 내역 ID |
| courseId | String | Y | 강의 ID |
| studentId | String | Y | 수강생 ID |
| amount | Number | Y | 결제 금액 |
| paidAt | OffsetDateTime | Y | 결제 일시 (예: 2025-03-05T10:00:00+09:00) |

**Request 예시**
```json
{
  "id": "sale-8",
  "courseId": "course-1",
  "studentId": "student-8",
  "amount": 50000,
  "paidAt": "2025-04-01T10:00:00+09:00"
}
```

**Response** `201 Created`
```json
{ "id": "sale-8" }
```

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| courseId가 존재하지 않음 | `"존재하지 않는 강의입니다: course-999"` |
| id가 이미 존재함 | `"이미 존재하는 판매 내역 ID입니다: sale-1"` |
| 필수 필드 누락 (`@NotBlank` / `@NotNull`) | 해당 필드의 검증 메시지 |
| `amount` ≤ 0 (`@Positive`) | 검증 메시지 |
| `paidAt` 형식 오류 (날짜만 입력 등) | `"요청 형식이 올바르지 않습니다."` |

---

## 4. 취소 내역 등록

```
POST /cancellation-records
```

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| saleRecordId | String | Y | 원본 판매 내역 ID |
| refundAmount | Number | Y | 환불 금액 (원결제금액과 달라도 됨 — 부분 환불 지원) |
| canceledAt | OffsetDateTime | Y | 취소 일시 |

**Request 예시**
```json
{
  "saleRecordId": "sale-3",
  "refundAmount": 80000,
  "canceledAt": "2025-03-25T10:00:00+09:00"
}
```

**Response** `201 Created`
```json
{ "id": 1 }
```

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| saleRecordId가 존재하지 않음 | `"존재하지 않는 판매 내역입니다: sale-999"` |
| 누적 환불 합계가 원결제 금액 초과 | `"누적 환불 금액이 원결제 금액을 초과합니다. 원결제: ..., 기존 환불 합계: ..., 요청 환불: ..."` |
| 필수 필드 누락 (`@NotBlank` / `@NotNull`) | 해당 필드의 검증 메시지 |
| `refundAmount` ≤ 0 (`@Positive`) | 검증 메시지 |
| `canceledAt` 형식 오류 | `"요청 형식이 올바르지 않습니다."` |

> **누적 환불 규칙:** `기존 환불 합계 + 이번 환불 요청 > 원결제 금액`이면 거절합니다. 부분 환불은 허용되며, 여러 번에 걸쳐 환불하더라도 합산 금액이 원결제를 넘으면 안 됩니다.

---

## 5. 판매 내역 목록 조회

```
GET /sale-records?creatorId={creatorId}&from={from}&to={to}
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| creatorId | String | Y | 크리에이터 ID |
| from | LocalDate | N | 조회 시작일 (예: 2025-03-01) |
| to | LocalDate | N | 조회 종료일 (예: 2025-03-31) |

- `from`, `to` 모두 없으면 해당 크리에이터의 전체 판매 내역 반환
- `from`만 또는 `to`만 입력하면 `400 Bad Request`
- `from`이 `to`보다 늦으면 `400 Bad Request`
- 기간 기준: `paidAt` (KST), `from`일 00:00:00 이상 ~ `to`일 다음날 00:00:00 미만 (종료일 당일 포함)

**Response** `200 OK`
```json
[
  {
    "id": "sale-1",
    "courseId": "course-1",
    "courseTitle": "Spring Boot 입문",
    "creatorId": "creator-1",
    "studentId": "student-1",
    "amount": 50000,
    "paidAt": "2025-03-05T10:00:00+09:00"
  }
]
```

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| `from`만 입력, `to` 없음 | `"from과 to는 함께 입력하거나 함께 생략해야 합니다."` |
| `to`만 입력, `from` 없음 | `"from과 to는 함께 입력하거나 함께 생략해야 합니다."` |
| `from`이 `to`보다 늦음 | `"시작일(from)은 종료일(to)보다 늦을 수 없습니다."` |

---

## 6. 크리에이터 월별 정산 조회 및 상태 관리

### 정산 상태 흐름

```
PENDING → CONFIRMED → PAID
```

| 상태 | 의미 | DB 레코드 |
|---|---|---|
| PENDING | 미확정 (운영자 미승인) | 없음 — GET 시 동적 계산 |
| CONFIRMED | 운영자 확정 완료, 스냅샷 저장 | 있음 |
| PAID | 지급 완료, paidAt 기록 | 있음 |

---

### GET — 정산 조회

```
GET /settlements/creators/{creatorId}?month={month}
```

- DB에 확정 레코드가 없으면 `PENDING` 상태로 동적 계산 반환
- CONFIRMED / PAID 이면 확정 시점의 스냅샷 반환 (이후 판매/취소 변동 미반영)

**Response** `200 OK`
```json
{
  "creatorId": "creator-1",
  "month": "2025-03",
  "status": "PENDING",
  "totalSales": 260000,
  "totalRefunds": 110000,
  "netSales": 150000,
  "platformFee": 30000,
  "settlementAmount": 120000,
  "saleCount": 4,
  "cancellationCount": 2,
  "confirmedAt": null,
  "paidAt": null
}
```

---

### PATCH — 상태 전환

```
PATCH /settlements/creators/{creatorId}?month={month}
```

**Request Body**

```json
{ "status": "CONFIRMED" }
```

| 요청 status | 동작 | 조건 |
|---|---|---|
| `CONFIRMED` | 정산 계산 → 스냅샷 저장 → CONFIRMED | 아직 레코드 없을 때만 |
| `PAID` | paidAt 기록 → PAID | 기존 상태가 CONFIRMED일 때만 |
| `PENDING` | 항상 400 | 직접 전환 불가 |

**에러 케이스**

| 조건 | 메시지 |
|---|---|
| 이미 CONFIRMED인데 CONFIRMED 요청 | `"이미 확정된 정산입니다: ..."` |
| PAID 상태에서 재전환 시도 | `"이미 지급 완료된 정산입니다: ..."` |
| CONFIRMED 없이 PAID 요청 | `"확정되지 않은 정산입니다. 먼저 CONFIRMED 처리가 필요합니다: ..."` |
| PENDING으로 직접 전환 요청 | `"PENDING으로 직접 전환할 수 없습니다."` |

**계산 공식**
```
순 판매액      = 총 판매 - 환불
플랫폼 수수료  = 순 판매 × 20% (원 단위 버림)
정산 예정 금액 = 순 판매 - 수수료
```

---

## 7. 운영자 기간별 집계 조회

```
GET /settlements/summary?from={from}&to={to}
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| from | LocalDate | Y | 시작일 (예: 2025-03-01) |
| to | LocalDate | Y | 종료일 (예: 2025-03-31) |

- 판매는 `paidAt` 기준, 취소는 `canceledAt` 기준으로 각각 필터
- 해당 기간에 판매 또는 취소가 있는 크리에이터만 포함

**Response** `200 OK`
```json
{
  "from": "2025-03-01",
  "to": "2025-03-31",
  "settlements": [
    {
      "creatorId": "creator-1",
      "creatorName": "김강사",
      "totalSales": 260000,
      "totalRefunds": 110000,
      "netSales": 150000,
      "platformFee": 30000,
      "settlementAmount": 120000,
      "saleCount": 4,
      "cancellationCount": 2
    },
    {
      "creatorId": "creator-2",
      "creatorName": "이강사",
      "totalSales": 60000,
      "totalRefunds": 0,
      "netSales": 60000,
      "platformFee": 12000,
      "settlementAmount": 48000,
      "saleCount": 1,
      "cancellationCount": 0
    }
  ],
  "totalSettlementAmount": 168000
}
```
