# 테스트 실행 방법

---

## 사전 준비

E2E 테스트는 실제 PostgreSQL DB에 연결합니다.
테스트 실행 전 Docker로 DB를 먼저 올려야 합니다.

```bash
docker-compose up -d      # PostgreSQL 실행
./gradlew test            # 전체 테스트 실행
```

특정 클래스만 실행:
```bash
./gradlew test --tests "com.example.sale.SaleRecordApiTest"
```

---

## 테스트 구조

```
src/test/java/com/example/
├── sale/
│   ├── SaleRecordApiTest.java          — 판매 내역 등록 (POST)
│   ├── SaleRecordListApiTest.java      — 판매 내역 목록 조회 (GET)
│   ├── SaleRecordValidationTest.java   — 판매 등록 입력값 검증
│   └── SaleRecordListBoundaryTest.java — 기간 필터 경계값 검증
├── cancellation/
│   ├── CancellationRecordApiTest.java  — 취소 내역 등록 (POST)
│   ├── CancellationRecordValidationTest.java — 취소 등록 입력값 검증
│   └── CancellationRefundRuleTest.java — 환불 금액 비즈니스 규칙 검증
└── settlement/
    ├── SettlementCalculationApiTest.java — 크리에이터 월별 정산 계산
    ├── SettlementSummaryApiTest.java     — 운영자 기간별 집계
    ├── SettlementEdgeCaseTest.java       — 정산 경계값 및 수수료 계산 검증
    └── SettlementStatusTest.java         — 정산 확정 상태 전이 (PENDING/CONFIRMED/PAID)
```

모든 테스트는 `@SpringBootTest` + `@Transactional` 조합으로 작성되었습니다.
각 테스트 후 변경 사항은 자동 롤백되어 테스트 간 데이터가 격리됩니다.
`DataInitializer`가 삽입한 샘플 데이터(sale-1~7, cancel-1~3)는 읽기 전용으로 공유됩니다.

---

## SaleRecordApiTest — 판매 내역 등록

**대상 API:** `POST /sale-records`

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| 판매_내역_정상_등록 | 유효한 요청 (id: test-sale-1, courseId: course-1) | `201` `{ "id": "test-sale-1" }` |
| 존재하지_않는_courseId_등록시_400 | courseId: course-999 (없는 강의) | `400` `{ "status": 400, "message": "존재하지 않는 강의입니다: course-999" }` |
| 중복_id_등록시_400 | id: sale-1 (이미 존재하는 ID) | `400` `{ "status": 400, "message": "이미 존재하는 판매 내역 ID입니다: sale-1" }` |

---

## SaleRecordListApiTest — 판매 내역 목록 조회

**대상 API:** `GET /sale-records?creatorId=&from=&to=`

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| 전체_판매_내역_조회 | creatorId: creator-1 (기간 없음) | `200` 배열 4건 (sale-1,2,3,4) |
| 기간_필터_판매_내역_조회 | creatorId: creator-1, from: 2025-03-01, to: 2025-03-31 | `200` 배열 4건 |
| 기간_필터_범위_밖_제외 | creatorId: creator-2, from: 2025-03-01, to: 2025-03-31 | `200` 배열 1건 (sale-6만 포함, sale-5는 1월 판매라 제외) |
| 응답_필드_확인 | creatorId: creator-1, 3월 기간 | `200` 각 항목에 courseId, courseTitle, creatorId, studentId, amount, paidAt 존재 확인 |
| 판매_내역_없는_크리에이터_조회 | creatorId: creator-1, from: 2024-01-01, to: 2024-01-31 | `200` 빈 배열 `[]` |

---

## SaleRecordValidationTest — 판매 등록 입력값 검증

**대상 API:** `POST /sale-records`

검증 실패 시 공통 응답: `400` `{ "status": 400, "message": "..." }`

### amount 검증

| 테스트 | 입력 | 이유 |
|---|---|---|
| amount_0이면_400 | amount: 0 | 0원 판매는 비즈니스상 허용하지 않음 (`@Positive`) |
| amount_음수이면_400 | amount: -1000 | 음수 금액 불가 |

### studentId 검증

| 테스트 | 입력 | 이유 |
|---|---|---|
| studentId_빈문자열이면_400 | studentId: "" | 빈 문자열 불가 (`@NotBlank`) |
| studentId_null이면_400 | studentId: null | null 불가 (`@NotBlank`) |

### courseId 검증

| 테스트 | 입력 | 이유 |
|---|---|---|
| courseId_빈문자열이면_400 | courseId: "" | 빈 문자열 불가 (`@NotBlank`) |
| courseId_null이면_400 | courseId: null | null 불가 (`@NotBlank`) |

### paidAt 검증

| 테스트 | 입력 | 이유 |
|---|---|---|
| paidAt_null이면_400 | paidAt: null | null 불가 (`@NotNull`) |
| paidAt_잘못된_형식이면_400 | paidAt: "2025-03-01" (날짜만, 시간대 없음) | OffsetDateTime 형식 불일치 → Jackson 파싱 실패 |

---

## CancellationRecordApiTest — 취소 내역 등록

**대상 API:** `POST /cancellation-records`

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| 취소_내역_정상_등록 | saleRecordId: sale-1, refundAmount: 50000 | `201` `{ "id": 숫자 }` (DB 자동 생성 ID) |
| 부분_환불_정상_등록 | saleRecordId: sale-4, refundAmount: 30000 (원결제 80,000보다 작음) | `201` `{ "id": 숫자 }` |
| 존재하지_않는_saleRecordId_등록시_400 | saleRecordId: sale-999 (없는 판매) | `400` `{ "status": 400, "message": "존재하지 않는 판매 내역입니다: sale-999" }` |

---

## CancellationRecordValidationTest — 취소 등록 입력값 검증

**대상 API:** `POST /cancellation-records`

검증 실패 시 공통 응답: `400` `{ "status": 400, "message": "..." }`

| 테스트 | 입력 | 이유 |
|---|---|---|
| refundAmount_음수이면_400 | refundAmount: -1000 | 음수 환불 금액 불가 (`@Positive`) |
| canceledAt_null이면_400 | canceledAt: null | null 불가 (`@NotNull`) |
| saleRecordId_빈문자열이면_400 | saleRecordId: "" | 빈 문자열 불가 (`@NotBlank`) |

---

## CancellationRefundRuleTest — 환불 금액 비즈니스 규칙 검증

**대상 API:** `POST /cancellation-records`

**핵심 규칙:** `기존 환불 합계 + 새 환불 요청 > 원결제 금액` 이면 거절

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 환불금액이_원결제초과시_400 | sale-1(50,000)에 60,000 환불 요청 | `400` "누적 환불 금액이 원결제 금액을 초과합니다" |
| 부분취소_여러번_등록_가능 | 20,000 → 30,000 순차 등록 (누적 50,000 = 원결제) | `201` × 2 |
| 누적환불액_원결제초과시_400 | 30,000 성공 후 30,000 추가 시도 (누적 60,000 > 50,000) | 1차 `201` / 2차 `400` |
| 전액환불_후_추가취소시_400 | sale-3 이미 cancel-1(80,000 전액환불) 존재, 1원 추가 시도 | `400` "누적 환불 금액이 원결제 금액을 초과합니다" |

---

## SettlementCalculationApiTest — 크리에이터 월별 정산 계산

**대상 API:** `GET /settlements/creators/{creatorId}?month={month}`

| 테스트 | 입력 | 기대 응답 핵심 |
|---|---|---|
| creator1_2025년3월_정산_계산 | creatorId: creator-1, month: 2025-03 | totalSales: 260,000 / totalRefunds: 110,000 / netSales: 150,000 / platformFee: 30,000 / settlementAmount: 120,000 / saleCount: 4 / cancellationCount: 2 |
| 부분_환불_차감_확인 | creatorId: creator-1, month: 2025-03 | totalRefunds: 110,000 (전액환불 80,000 + 부분환불 30,000) |
| 월경계_1월말_판매는_1월_정산에_포함 | creatorId: creator-2, month: 2025-01 | totalSales: 60,000 / totalRefunds: 0 (cancel-3은 2월 취소라 미포함) |
| 월경계_2월초_취소는_2월_정산에_반영 | creatorId: creator-2, month: 2025-02 | totalSales: 0 (sale-5는 1월 판매라 미포함) / totalRefunds: 60,000 / netSales: -60,000 |
| 빈_월_조회시_모두_0 | creatorId: creator-3, month: 2025-03 | 전 필드 0, saleCount: 0, cancellationCount: 0 |
| 수수료_계산_정확성 | creatorId: creator-2, month: 2025-03 | netSales: 60,000 → platformFee: 12,000 (20% 버림) → settlementAmount: 48,000 |

---

## SettlementSummaryApiTest — 운영자 기간별 집계

**대상 API:** `GET /settlements/summary?from={from}&to={to}`

| 테스트 | 입력 | 기대 응답 핵심 |
|---|---|---|
| 크리에이터별_정산_목록_및_전체_합계 | from: 2025-03-01, to: 2025-03-31 | settlements 2건 / totalSettlementAmount: 168,000 |
| 크리에이터별_정산_예정_금액_정확성 | from: 2025-03-01, to: 2025-03-31 | creator-1(settlementAmount: 120,000, creatorName: 김강사) / creator-2(settlementAmount: 48,000, creatorName: 이강사) |
| 데이터_없는_크리에이터_제외 | from: 2025-03-01, to: 2025-03-31 | creator-3 미포함 (3월 데이터 없음) |
| 기간_필터_적용 | from: 2025-02-01, to: 2025-02-28 | settlements 2건 / creator-1 미포함 (2월 데이터 없음) |
| 판매_취소_기준_필드_독립_집계 | from: 2025-02-01, to: 2025-02-28 | creator-2: totalSales: 0 / totalRefunds: 60,000 / netSales: -60,000 (sale-5는 1월 판매라 미포함, cancel-3은 2월 취소라 포함) |
| 데이터_없는_기간_조회 | from: 2024-01-01, to: 2024-01-31 | settlements: [] / totalSettlementAmount: 0 |
| 순판매_음수_creator_summary에_포함 | from: 2025-02-01, to: 2025-02-28 | netSales=-60,000인 creator-2가 목록에 포함 / settlementAmount: -48,000 |
| 여러_course_판매가_creator_단위로_합산된다 | from: 2025-03-01, to: 2025-03-31 | creator-1(course-1+course-2) → 항목 1건 / totalSales: 260,000 / saleCount: 4 / totalRefunds: 110,000 / cancellationCount: 2 |

---

---

## SaleRecordListBoundaryTest — 판매 내역 기간 필터 경계값

**대상 API:** `GET /sale-records?creatorId=&from=&to=`

### 시작일 경계

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| 시작일_당일_paidAt_포함 | from=2025-03-05, to=2025-03-05 (sale-1 paidAt=2025-03-05T10:00) | `200` 1건, sale-1 포함 |
| 시작일_하루전_paidAt_제외 | from=2025-03-06, to=2025-03-31 | `200` sale-1 미포함 |

### 종료일 경계

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| 종료일_당일_paidAt_포함 | from=2025-03-22, to=2025-03-22 (sale-4 paidAt=2025-03-22T11:00) | `200` 1건, sale-4 포함 |
| 종료일_하루뒤_paidAt_제외 | from=2025-03-01, to=2025-03-21 | `200` sale-4 미포함 |

### 파라미터 유효성

| 테스트 | 입력 | 기대 응답 |
|---|---|---|
| from이_to보다_늦으면_400 | from=2025-03-31, to=2025-03-01 | `400` `{ "status": 400, "message": "시작일(from)은 종료일(to)보다 늦을 수 없습니다." }` |
| from만_입력시_400 | from=2025-03-01 (to 없음) | `400` `{ "status": 400, "message": "from과 to는 함께 입력하거나 함께 생략해야 합니다." }` |
| to만_입력시_400 | to=2025-03-31 (from 없음) | `400` `{ "status": 400, "message": "from과 to는 함께 입력하거나 함께 생략해야 합니다." }` |

> **경계 정의:** from → `from일 00:00:00 KST` 이상 / to → `(to+1)일 00:00:00 KST` 미만 (종료일 당일 포함)

---

## SettlementEdgeCaseTest — 정산 경계값 및 수수료 계산 검증

**대상 API:** `GET /settlements/creators/{creatorId}?month=` / `GET /settlements/summary?from=&to=`

> 기본 시나리오 테스트(SettlementCalculationApiTest/SettlementSummaryApiTest)에서 다루지 않는 수치 경계를 검증합니다.  
> 테스트 내부에서 `POST /sale-records` + `POST /cancellation-records`로 데이터를 직접 세팅합니다 (2025-04, creator-3/course-4).

### 순판매금액 = 0

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 순판매금액이_0이면_수수료와_정산금액도_0 | 50,000 판매 → 50,000 전액환불 | netSales: 0 / platformFee: 0 / settlementAmount: 0 |

### 소수점 버림 (RoundingMode.DOWN)

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 수수료_소수점_버림_999원 | netSales=999 → 999 × 20% = 199.8 | platformFee: **199** (반올림이면 200 — 버림 검증) |
| 수수료_소수점_버림_1001원 | netSales=1001 → 1001 × 20% = 200.2 | platformFee: **200** (올림이면 201 — 버림 검증) |

### 순판매금액 음수 시 수수료

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 순판매금액_음수일때_수수료_확인 | creator-2, 2025-02: sale=0 / refund=60,000 | netSales: -60,000 / platformFee: **-12,000** / settlementAmount: -48,000 |

> 음수 netSales에 20%를 곱하면 fee도 음수가 되어 settlementAmount에서 환원됩니다 (DOWN은 0 방향 버림).

### summary 포함 여부

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 순판매금액_0인_크리에이터_summary에_포함 | 판매+전액환불로 settlementAmount=0 | settlements 1건 포함 / totalSettlementAmount: 0 |

### 여러 건 합산 정확성

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 여러건_판매_여러건_부분환불_합산_정확성 | 판매 3건(10,000+20,000+30,000) + 부분환불 2건(5,000+7,000) | totalSales: 60,000 / totalRefunds: 12,000 / netSales: 48,000 / platformFee: 9,600 / settlementAmount: 38,400 / saleCount: 3 / cancellationCount: 2 |

---

## SettlementStatusTest — 정산 확정 상태 전이

**대상 API:** `GET /settlements/creators/{creatorId}?month=` / `PATCH /settlements/creators/{creatorId}?month=`

### PENDING (기본 상태)

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| 확정전_GET_조회는_PENDING_반환 | 레코드 없이 GET 조회 | status: PENDING / confirmedAt 없음 / paidAt 없음 |

### CONFIRMED 정상 전환

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| CONFIRMED_요청시_스냅샷_저장 | `{ "status": "CONFIRMED" }` | status: CONFIRMED / 금액 스냅샷 저장 / confirmedAt 존재 |
| CONFIRMED_후_GET_조회는_스냅샷_반환 | CONFIRMED 후 GET | status: CONFIRMED / settlementAmount 스냅샷 값 반환 |

### PAID 정상 전환

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| CONFIRMED_후_PAID_전환_성공 | CONFIRMED → `{ "status": "PAID" }` | status: PAID / confirmedAt + paidAt 모두 존재 |
| PAID_후_GET_조회 | PAID 후 GET | status: PAID / paidAt 존재 |

### 중복 정산 방지

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| CONFIRMED_중복_요청시_400 | CONFIRMED → CONFIRMED 재요청 | `400` "이미 확정된 정산입니다" |
| PAID_후_CONFIRMED_재요청시_400 | PAID 후 CONFIRMED 재요청 | `400` "이미 지급 완료된 정산입니다" |

### 잘못된 전이 순서

| 테스트 | 시나리오 | 기대 응답 |
|---|---|---|
| CONFIRMED_없이_PAID_요청시_400 | 레코드 없이 PAID 요청 | `400` "확정되지 않은 정산입니다" |
| PAID_후_PAID_재요청시_400 | PAID → PAID 재요청 | `400` "이미 지급 완료된 정산입니다" (재지급 방지) |
| PENDING_직접_전환_요청시_400 | `{ "status": "PENDING" }` 직접 요청 | `400` "PENDING으로 직접 전환할 수 없습니다." |

---

## 전체 테스트 실행 결과 요약

| 테스트 클래스 | 테스트 수 |
|---|---|
| SaleRecordApiTest | 3 |
| SaleRecordListApiTest | 5 |
| SaleRecordValidationTest | 8 |
| SaleRecordListBoundaryTest | 7 |
| CancellationRecordApiTest | 3 |
| CancellationRecordValidationTest | 3 |
| CancellationRefundRuleTest | 4 |
| SettlementCalculationApiTest | 6 |
| SettlementSummaryApiTest | 8 |
| SettlementEdgeCaseTest | 7 |
| SettlementStatusTest | 11 |
| **합계** | **65** |
