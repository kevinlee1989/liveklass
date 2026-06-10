package com.example.settlement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Settlement {

    private Long id;
    private String creatorId;
    private String month;
    private SettlementStatus status;
    private BigDecimal totalSales;
    private BigDecimal totalRefunds;
    private BigDecimal netSales;
    private BigDecimal platformFee;
    private BigDecimal settlementAmount;
    private int saleCount;
    private int cancellationCount;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime paidAt;

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
