package com.example.settlement.dto;

import com.example.settlement.SettlementStatus;
import jakarta.validation.constraints.NotNull;

public record SettlementStatusRequest(
        @NotNull SettlementStatus status
) {
}
