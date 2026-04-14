# 테스트 실행 방법

---

## 사전 준비

앱이 실행 중이어야 합니다. 실행 방법은 [README.md](../README.md)를 참고하세요.

```bash
docker-compose up -d
./gradlew bootRun
```

앱 시작 시 샘플 데이터가 자동 삽입됩니다.

---

## 과제 검증 시나리오

### 시나리오 1 — creator-1의 2025-03 정산

```
GET http://localhost:8080/settlements/creators/creator-1?month=2025-03
```

**기댓값**
```json
{
  "totalSales": 260000,
  "totalRefunds": 110000,
  "netSales": 150000,
  "platformFee": 30000,
  "settlementAmount": 120000,
  "saleCount": 4,
  "cancellationCount": 2
}
```

---

### 시나리오 2 — 부분 환불 처리 확인

sale-4(80,000원)에 대해 30,000원만 환불된 cancel-2가 정산에 반영되는지 확인.
시나리오 1 결과에서 `totalRefunds: 110000` (= 80,000 + 30,000)으로 확인.

---

### 시나리오 3 — 월 경계 취소 (1월 판매 / 2월 취소)

```
# sale-5는 1월 판매 → 1월 정산에 포함
GET http://localhost:8080/settlements/creators/creator-2?month=2025-01
```

**기댓값**: `totalSales: 60000`, `totalRefunds: 0` (취소는 2월에 발생)

```
# cancel-3은 2월 취소 → 2월 정산에 환불로 반영
GET http://localhost:8080/settlements/creators/creator-2?month=2025-02
```

**기댓값**: `totalSales: 0`, `totalRefunds: 60000`

---

### 시나리오 4 — 빈 월 조회 (데이터 없음)

```
GET http://localhost:8080/settlements/creators/creator-3?month=2025-03
```

**기댓값**: 모든 금액 0, 건수 0

---

### 시나리오 5 — 운영자 집계

```
GET http://localhost:8080/settlements/summary?from=2025-03-01&to=2025-03-31
```

**기댓값**: creator-1, creator-2 포함, creator-3 미포함 (3월 데이터 없음)

---

### 시나리오 6 — 판매 내역 목록 조회

```
# 기간 필터 없이 전체
GET http://localhost:8080/sale-records?creatorId=creator-1

# 기간 필터 포함
GET http://localhost:8080/sale-records?creatorId=creator-1&from=2025-03-01&to=2025-03-31
```

---

### 시나리오 7 — 판매 등록 후 정산 반영 확인

```
# 1. 새 판매 등록
POST http://localhost:8080/sale-records
{
  "id": "sale-new",
  "courseId": "course-1",
  "studentId": "student-99",
  "amount": 50000,
  "paidAt": "2025-03-30T10:00:00+09:00"
}

# 2. 정산 재조회 → totalSales가 50,000 증가했는지 확인
GET http://localhost:8080/settlements/creators/creator-1?month=2025-03
```

---

### 시나리오 8 — 존재하지 않는 ID 에러 처리

```
# 없는 courseId로 판매 등록 → 400 에러 확인
POST http://localhost:8080/sale-records
{
  "id": "sale-err",
  "courseId": "course-999",
  "studentId": "student-1",
  "amount": 10000,
  "paidAt": "2025-03-01T10:00:00+09:00"
}
```
