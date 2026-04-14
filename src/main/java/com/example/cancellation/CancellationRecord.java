package com.example.cancellation;

import com.example.sale.SaleRecord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cancellation_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CancellationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_record_id", nullable = false)
    private SaleRecord saleRecord;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "canceled_at", nullable = false)
    private OffsetDateTime canceledAt;

    public static CancellationRecord of(SaleRecord saleRecord, BigDecimal refundAmount, OffsetDateTime canceledAt) {
        CancellationRecord record = new CancellationRecord();
        record.saleRecord = saleRecord;
        record.refundAmount = refundAmount;
        record.canceledAt = canceledAt;
        return record;
    }
}
