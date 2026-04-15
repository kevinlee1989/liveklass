package com.example.cancellation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CancellationRecordRequest(
        @NotBlank String saleRecordId,
        @NotNull @Positive BigDecimal refundAmount,
        @NotNull OffsetDateTime canceledAt
) {
}
