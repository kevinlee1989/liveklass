package com.example.sale.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleRecordRequest(
        String id,
        String courseId,
        String studentId,
        BigDecimal amount,
        OffsetDateTime paidAt
) {
}
