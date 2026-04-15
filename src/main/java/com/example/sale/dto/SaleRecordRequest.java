package com.example.sale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleRecordRequest(
        @NotBlank String id,
        @NotBlank String courseId,
        @NotBlank String studentId,
        @NotNull @Positive BigDecimal amount,
        @NotNull OffsetDateTime paidAt
) {
}
