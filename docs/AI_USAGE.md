# AI 활용 범위

---

## 사용 도구

- **Claude Code (claude-sonnet-4-6)** — Anthropic CLI 기반 AI 코딩 어시스턴트

---

## 활용 내역

### 코드 생성

| 항목 | AI 기여 수준 | 설명 |
|---|---|---|
| 엔티티 설계 검토 | 보조 | 기존 엔티티 구조 검토 및 불필요한 creatorId 필드 제거 제안 |
| DataInitializer | 생성 | 샘플 데이터 삽입 코드 전체 생성 |
| SaleRecord 등록 API | 생성 | Controller / Service / DTO 생성 |
| CancellationRecord 등록 API | 생성 | Controller / Service / DTO 생성 |
| 판매 내역 목록 조회 API | 생성 | 기간 필터 분기 처리, from/to 파라미터 유효성 검증 포함 |
| 정산 계산 로직 | 생성 | SettlementService의 KST 월 경계 계산 및 금액 계산 로직 |
| 운영자 집계 API | 생성 | 크리에이터별 그룹핑 및 합계 계산 로직 |
| Creator / Course 등록 API | 생성 | POST /creators, POST /courses 전체 생성 |
| 정산 확정 상태 관리 | 생성 | Settlement 엔티티, SettlementStatus enum, PATCH API, 스냅샷 저장 로직 |
| 글로벌 예외 핸들러 | 생성 | GlobalExceptionHandler (@RestControllerAdvice) |
| 패키지 구조 리팩토링 | 보조 | domain/ 제거 후 도메인 중심 구조로 파일 이동 |
| 문서 작성 | 생성 | README.md 및 docs/ 전체 (API.md, DATA_MODEL.md, DECISIONS.md, TESTING.md, CONSTRAINTS.md) |

### 테스트 코드 생성

| 테스트 클래스 | AI 기여 수준 | 설명 |
|---|---|---|
| SaleRecordApiTest | 생성 | 판매 등록 정상/오류 시나리오 |
| SaleRecordListApiTest | 생성 | 판매 목록 조회 5개 시나리오 |
| SaleRecordValidationTest | 생성 | 입력값 검증 8개 케이스 |
| SaleRecordListBoundaryTest | 생성 | 기간 필터 경계값 7개 케이스 (시작일/종료일 당일 포함 여부, from>to, 단독 입력) |
| CancellationRecordApiTest | 생성 | 취소 등록 정상/오류 시나리오 |
| CancellationRecordValidationTest | 생성 | 취소 입력값 검증 3개 케이스 |
| CancellationRefundRuleTest | 생성 | 누적 환불 초과 방지 4개 케이스 |
| SettlementCalculationApiTest | 생성 | 과제 시나리오 기반 정산 계산 6개 케이스 |
| SettlementSummaryApiTest | 생성 | 운영자 집계 API 8개 케이스 (여러 course 합산, 음수 creator 포함 여부 포함) |
| SettlementEdgeCaseTest | 생성 | 정산 경계값 7개 케이스 (netSales=0, 소수점 버림, 음수 수수료, 여러 건 합산) |

### 설계 결정에서의 역할

- 월 경계를 `exclusive upper bound` 방식으로 처리하는 방법 제안 (`to+1일 00:00:00 미만`)
- `paidAt` / `canceledAt` 분리 기준으로 정산 집계하는 구조 설계
- 수수료율 상수 분리로 변경 가능성 반영 (`FEE_RATE`)
- PENDING 상태를 DB에 저장하지 않는 설계 제안 (GET 시 동적 계산)
- 정산 확정 시점의 전체 금액 필드를 스냅샷으로 저장하는 방식 제안
- `RoundingMode.DOWN`으로 수수료 버림 처리 제안
- Jackson `non_null` 설정으로 null 필드 응답 제외 제안

---

## 직접 작성 / 결정한 부분

- 프로젝트 초기 엔티티 구조 (Creator, Course, SaleRecord, CancellationRecord)
- docker-compose.yml
- application.properties DB 설정
- 전체 아키텍처 방향 결정 (도메인 중심 패키지, Spring Boot 기술 스택 선택)
- 정산 상태 관리 설계 방향 결정 (PENDING/CONFIRMED/PAID 구분, 스냅샷 방식 채택)
- 추가 테스트 케이스 항목 결정 (어떤 경계값을 검증할지, 어떤 edge case가 중요한지)

---

## 유의사항

AI가 생성한 코드는 모두 직접 검토하고 빌드 확인 및 테스트 실행을 거쳤습니다.
비즈니스 로직(정산 계산 공식, 월 경계 정의, 상태 전이 규칙 등)은 과제 요구사항을 기준으로 직접 검증했습니다.
테스트 케이스에서 발견된 오류(JSONPath 문법 오류, MockMvc 설정 문제 등)는 직접 원인을 분석하고 수정했습니다.
