package com.example.settlement.dto;

import java.math.BigDecimal;
import java.util.List;

public record SettlementSummaryResponse(
        String from,
        String to,
        List<CreatorSettlementSummary> settlements,
        BigDecimal totalSettlementAmount
) {
}
