package com.example.settlement;

public enum SettlementStatus {
    PENDING,    // 정산 미확정 (DB에 레코드 없음 - 동적 계산)
    CONFIRMED,  // 운영자 확정 완료 (스냅샷 저장, 이후 금액 변경 불가)
    PAID        // 지급 완료 (paidAt 기록, 재지급 방지)
}
