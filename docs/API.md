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
| `400 Bad Request` | 요청값이 유효하지 않음 (존재하지 않는 ID 등) |
| `500 Internal Server Error` | 서버 내부 오류 |

---

## 1. 판매 내역 등록

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

**에러** `400 Bad Request` — courseId가 존재하지 않을 경우
```json
{ "status": 400, "message": "존재하지 않는 강의입니다: course-999" }
```

---

## 2. 취소 내역 등록

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

**에러** `400 Bad Request` — saleRecordId가 존재하지 않을 경우
```json
{ "status": 400, "message": "존재하지 않는 판매 내역입니다: sale-999" }
```

---

## 3. 판매 내역 목록 조회

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
- 기간 기준: `paidAt` (KST)

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

---

## 4. 크리에이터 월별 정산 조회

```
GET /settlements/creators/{creatorId}?month={month}
```

**Path Parameter**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| creatorId | String | 크리에이터 ID |

**Query Parameter**

| 파라미터 | 타입 | 설명 |
|---|---|---|
| month | String | 조회 연월 (예: 2025-03) |

**Response** `200 OK`
```json
{
  "creatorId": "creator-1",
  "month": "2025-03",
  "totalSales": 260000,
  "totalRefunds": 110000,
  "netSales": 150000,
  "platformFee": 30000,
  "settlementAmount": 120000,
  "saleCount": 4,
  "cancellationCount": 2
}
```

**계산 공식**
```
순 판매액      = 총 판매 - 환불
플랫폼 수수료  = 순 판매 × 20% (원 단위 버림)
정산 예정 금액 = 순 판매 - 수수료
```

---

## 5. 운영자 기간별 집계 조회

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
