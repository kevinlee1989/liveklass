package com.example.settlement.dto;

import java.math.BigDecimal;

public record MonthlySettlementResponse(
        String creatorId,
        String month,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal netSales,
        BigDecimal platformFee,
        BigDecimal settlementAmount,
        int saleCount,
        int cancellationCount
) {
}
