# AI 활용 범위

---

## 사용 도구

- **Claude Code (claude-sonnet-4-6)** — Anthropic CLI 기반 AI 코딩 어시스턴트

---

## 활용 내역

### 코드 생성

| 항목 | AI 기여 수준 | 설명 |
|---|---|---|
| 엔티티 설계 검토 | 보조 | 기존 엔티티 구조 검토 및 creatorId 필드 제거 제안 |
| DataInitializer | 생성 | 샘플 데이터 삽입 코드 전체 생성 |
| SaleRecord 등록 API | 생성 | Controller / Service / DTO 생성 |
| CancellationRecord 등록 API | 생성 | Controller / Service / DTO 생성 |
| 정산 계산 로직 | 생성 | SettlementService의 KST 월 경계 계산 및 금액 계산 로직 |
| 운영자 집계 API | 생성 | 크리에이터별 그룹핑 및 합계 계산 로직 |
| 판매 내역 목록 조회 | 생성 | 기간 필터 분기 처리 포함 |
| 패키지 구조 리팩토링 | 보조 | domain/ 제거 후 도메인 중심 구조로 파일 이동 |
| 문서 작성 | 생성 | README.md 및 docs/ 전체 |

### 설계 결정에서의 역할

- 월 경계를 `exclusive upper bound` 방식으로 처리하는 방법 제안
- `paidAt` / `canceledAt` 분리 기준으로 정산 집계하는 구조 설계
- 수수료율 상수 분리로 변경 가능성 반영

---

## 직접 작성한 부분

- 프로젝트 초기 엔티티 구조 (Creator, Course, SaleRecord, CancellationRecord)
- docker-compose.yml
- application.properties DB 설정
- 전체 아키텍처 방향 결정 (도메인 중심 패키지, Spring Boot 기술 스택 선택)

---

## 유의사항

AI가 생성한 코드는 모두 직접 검토하고 빌드 확인을 거쳤습니다.
비즈니스 로직(정산 계산 공식, 월 경계 정의 등)은 과제 요구사항을 기준으로 직접 검증했습니다.
