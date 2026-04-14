# 설계 결정과 이유

---

## 요구사항 해석 및 가정

### 월 경계 기준
- **해석**: "KST 기준 해당 월 1일 00:00:00 ~ 말일 23:59:59"를 코드상에서는 `1일 00:00:00 이상 ~ 다음달 1일 00:00:00 미만`으로 처리
- **이유**: 말일 23:59:59 기준은 윤초 등 엣지 케이스가 발생할 수 있어 exclusive upper bound 방식이 안전

### 취소는 원결제 금액과 달라도 된다
- **해석**: `CancellationRecord.refundAmount`는 원 `SaleRecord.amount`와 독립적인 값
- **이유**: 부분 환불(케이스 4, sale-4의 80,000원 중 30,000원만 환불) 시나리오를 처리하기 위함

### 판매가 없는 월 조회
- **해석**: 데이터가 없으면 모든 금액 필드를 0으로 응답
- **이유**: null 응답은 클라이언트에서 예외 처리가 필요해지므로 일관된 0 응답이 더 안전

### 운영자 집계에서 크리에이터 범위
- **해석**: 해당 기간에 판매 또는 취소 중 하나라도 있으면 응답에 포함
- **이유**: 판매 없이 취소만 있는 경우(이월 판매의 당월 취소)도 집계에 반영해야 함

---

## 설계 결정

### 1. 도메인 중심 패키지 구조

```
sale/, cancellation/, creator/, course/, settlement/
```

레이어 중심 구조(`controller/`, `service/`, `repository/`) 대신 도메인 중심으로 구성.

**이유**: 기능 단위로 파일이 모여 있어 관련 코드를 찾기 쉽고 응집도가 높음

---

### 2. 수수료율 상수 분리

```java
private static final BigDecimal FEE_RATE = new BigDecimal("0.20");
```

**이유**: 과제 조건에서 "변경 가능성을 설계에 반영하면 가산점"으로 명시. 상수로 분리해 추후 DB 관리 방식으로 교체가 용이하도록 설계

---

### 3. 판매/취소 기준 필드 분리

정산 계산 시 판매는 `paidAt`, 취소는 `canceledAt`을 각각 독립적으로 사용.

**이유**: 케이스 5처럼 1월에 판매하고 2월에 취소한 경우, 판매는 1월 정산에, 취소는 2월 정산에 반영되어야 함. 같은 필드를 사용하면 이 케이스를 처리할 수 없음

---

### 4. CancellationRecord ID는 DB 자동 생성 (Long)

SaleRecord, Creator, Course는 외부에서 ID를 지정하지만, CancellationRecord는 `@GeneratedValue`로 DB가 자동 생성.

**이유**: 취소 내역은 외부 시스템에서 특정 ID로 참조될 필요가 없고, 원본 판매 내역(`saleRecordId`)으로 충분히 식별 가능

---

### 5. 수수료 계산 시 RoundingMode.DOWN

```java
netSales.multiply(FEE_RATE).setScale(0, RoundingMode.DOWN)
```

**이유**: 수수료를 올림 처리하면 크리에이터에게 불리하므로, 원 단위 버림(내림) 적용

---

### 6. DataInitializer로 샘플 데이터 자동 삽입

`ApplicationRunner`를 구현한 `DataInitializer`가 앱 시작 시 과제 샘플 데이터를 자동 삽입.

**이유**: `ddl-auto=create-drop`으로 앱 재시작마다 테이블이 초기화되므로, 매번 수동으로 데이터를 넣지 않아도 즉시 테스트 가능
