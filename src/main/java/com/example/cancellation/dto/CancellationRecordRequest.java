package com.example.cancellation.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CancellationRecordRequest(
        String saleRecordId,
        BigDecimal refundAmount,
        OffsetDateTime canceledAt
) {
}
