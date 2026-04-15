package com.example.settlement.dto;

import com.example.settlement.Settlement;
import com.example.settlement.SettlementStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MonthlySettlementResponse(
        String creatorId,
        String month,
        SettlementStatus status,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal netSales,
        BigDecimal platformFee,
        BigDecimal settlementAmount,
        int saleCount,
        int cancellationCount,
        OffsetDateTime confirmedAt,
        OffsetDateTime paidAt
) {
    // PENDING: 동적 계산 결과 (DB 레코드 없음)
    public static MonthlySettlementResponse pending(
            String creatorId, String month,
            BigDecimal totalSales, BigDecimal totalRefunds,
            BigDecimal netSales, BigDecimal platformFee,
            BigDecimal settlementAmount, int saleCount, int cancellationCount
    ) {
        return new MonthlySettlementResponse(
                creatorId, month, SettlementStatus.PENDING,
                totalSales, totalRefunds, netSales, platformFee, settlementAmount,
                saleCount, cancellationCount, null, null
        );
    }

    // CONFIRMED / PAID: 스냅샷에서 복원
    public static MonthlySettlementResponse from(Settlement s) {
        return new MonthlySettlementResponse(
                s.getCreatorId(), s.getMonth(), s.getStatus(),
                s.getTotalSales(), s.getTotalRefunds(), s.getNetSales(),
                s.getPlatformFee(), s.getSettlementAmount(),
                s.getSaleCount(), s.getCancellationCount(),
                s.getConfirmedAt(), s.getPaidAt()
        );
    }
}
