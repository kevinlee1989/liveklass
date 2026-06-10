package com.example.cancellation;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class CancellationRecord {

    private Long id;
    private String saleRecordId;
    private BigDecimal refundAmount;
    private OffsetDateTime canceledAt;

    public static CancellationRecord of(String saleRecordId, BigDecimal refundAmount, OffsetDateTime canceledAt) {
        CancellationRecord record = new CancellationRecord();
        record.saleRecordId = saleRecordId;
        record.refundAmount = refundAmount;
        record.canceledAt = canceledAt;
        return record;
    }
}
