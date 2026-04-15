# 미구현 / 제약사항

---

## 미구현 항목

### 선택 구현 (가산점) 항목

| 항목 | 상태 | 비고 |
|---|---|---|
| 정산 확정 상태 관리 (PENDING → CONFIRMED → PAID) | 미구현 | |
| 동일 기간 중복 정산 방지 로직 | 미구현 | |
| 정산 내역 CSV 다운로드 | 미구현 | |
| 수수료율 변경 이력 관리 | 미구현 | 수수료율은 코드 상수(20%)로 고정 |

### 기타 미구현 항목

| 항목 | 비고 |
|---|---|
| Course 제목 수정 / 삭제 API | 등록만 가능, 수정·삭제 없음 |
| 페이지네이션 | 판매 목록 조회 시 전체 반환 |

---

## 구현된 항목 (초기 미구현에서 전환)

| 항목 | 구현 방식 |
|---|---|
| 입력값 유효성 검증 | DTO에 `@NotBlank` / `@NotNull` / `@Positive` + Controller에 `@Valid` 적용 |
| 글로벌 예외 핸들러 | `GlobalExceptionHandler` (`@RestControllerAdvice`) — `IllegalArgumentException` → 400, `MethodArgumentNotValidException` → 400, `HttpMessageNotReadableException` → 400, 그 외 → 500 |
| 누적 환불 초과 방지 | `CancellationRecordService`에서 기존 환불 합계 조회 후 서비스 레이어 검증 |
| 중복 판매 ID 방지 | `SaleRecordService`에서 `existsById` 선조회 후 중복 시 400 반환 |

---

## 제약사항

### DB 스키마 자동 관리
`spring.jpa.hibernate.ddl-auto=create-drop` 설정으로 앱 재시작 시 테이블이 초기화됩니다.
데이터를 영구 보존하려면 `update` 또는 별도 마이그레이션 도구(Flyway 등)로 전환이 필요합니다.

### 수수료율 고정
수수료율은 `SettlementService`의 상수(`FEE_RATE = 0.20`)로 관리됩니다.
운영 중 수수료율 변경은 코드 수정 후 재배포가 필요합니다.

### 인증/인가 없음
모든 API가 인증 없이 접근 가능합니다.
운영자 전용 API(`/settlements/summary`)도 별도 권한 제어가 없습니다.
