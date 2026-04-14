package com.example.settlement.dto;

import java.math.BigDecimal;

public record CreatorSettlementSummary(
        String creatorId,
        String creatorName,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal netSales,
        BigDecimal platformFee,
        BigDecimal settlementAmount,
        int saleCount,
        int cancellationCount
) {
}
