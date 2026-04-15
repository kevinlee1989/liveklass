package com.example.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "settlements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"creator_id", "month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "month", nullable = false)
    private String month;  // "2025-03" 형식

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    // ── 스냅샷 (CONFIRMED 시점 기준) ──────────────────────────────
    @Column(nullable = false)
    private BigDecimal totalSales;

    @Column(nullable = false)
    private BigDecimal totalRefunds;

    @Column(nullable = false)
    private BigDecimal netSales;

    @Column(nullable = false)
    private BigDecimal platformFee;

    @Column(nullable = false)
    private BigDecimal settlementAmount;

    @Column(nullable = false)
    private int saleCount;

    @Column(nullable = false)
    private int cancellationCount;

    // ── 상태 타임스탬프 ───────────────────────────────────────────
    @Column(nullable = false)
    private OffsetDateTime confirmedAt;

    @Column
    private OffsetDateTime paidAt;  // PAID 전환 시 기록

    // ── 생성 / 전이 ───────────────────────────────────────────────

    public static Settlement confirm(
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
        Settlement s = new Settlement();
        s.creatorId = creatorId;
        s.month = month;
        s.status = SettlementStatus.CONFIRMED;
        s.totalSales = totalSales;
        s.totalRefunds = totalRefunds;
        s.netSales = netSales;
        s.platformFee = platformFee;
        s.settlementAmount = settlementAmount;
        s.saleCount = saleCount;
        s.cancellationCount = cancellationCount;
        s.confirmedAt = OffsetDateTime.now();
        return s;
    }

    public void markAsPaid() {
        this.status = SettlementStatus.PAID;
        this.paidAt = OffsetDateTime.now();
    }
}
