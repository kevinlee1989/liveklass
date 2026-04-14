package com.example.settlement;

import com.example.cancellation.CancellationRecord;
import com.example.cancellation.CancellationRecordRepository;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordRepository;
import com.example.settlement.dto.CreatorSettlementSummary;
import com.example.settlement.dto.MonthlySettlementResponse;
import com.example.settlement.dto.SettlementSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.20");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final SaleRecordRepository saleRecordRepository;
    private final CancellationRecordRepository cancellationRecordRepository;

    public MonthlySettlementResponse calculate(String creatorId, YearMonth month) {
        OffsetDateTime from = month.atDay(1).atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime to = month.plusMonths(1).atDay(1).atStartOfDay(KST).toOffsetDateTime();

        List<SaleRecord> sales = saleRecordRepository.findByCreatorAndPaidAtBetween(creatorId, from, to);
        List<CancellationRecord> cancellations = cancellationRecordRepository.findByCreatorAndCanceledAtBetween(creatorId, from, to);

        BigDecimal totalSales = sum(sales.stream().map(SaleRecord::getAmount).toList());
        BigDecimal totalRefunds = sum(cancellations.stream().map(CancellationRecord::getRefundAmount).toList());
        BigDecimal netSales = totalSales.subtract(totalRefunds);
        BigDecimal platformFee = fee(netSales);
        BigDecimal settlementAmount = netSales.subtract(platformFee);

        return new MonthlySettlementResponse(
                creatorId,
                month.toString(),
                totalSales,
                totalRefunds,
                netSales,
                platformFee,
                settlementAmount,
                sales.size(),
                cancellations.size()
        );
    }

    public SettlementSummaryResponse summarize(LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = from.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay(KST).toOffsetDateTime();

        List<SaleRecord> sales = saleRecordRepository.findByPaidAtBetween(fromDt, toDt);
        List<CancellationRecord> cancellations = cancellationRecordRepository.findByCanceledAtBetween(fromDt, toDt);

        Map<String, List<SaleRecord>> salesByCreator = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getCourse().getCreator().getId()));

        Map<String, List<CancellationRecord>> cancellationsByCreator = cancellations.stream()
                .collect(Collectors.groupingBy(c -> c.getSaleRecord().getCourse().getCreator().getId()));

        // 판매 또는 취소가 있는 전체 크리에이터
        Set<String> allCreatorIds = new HashSet<>();
        allCreatorIds.addAll(salesByCreator.keySet());
        allCreatorIds.addAll(cancellationsByCreator.keySet());

        // 크리에이터 이름 수집
        Map<String, String> creatorNames = new HashMap<>();
        sales.forEach(s -> creatorNames.put(
                s.getCourse().getCreator().getId(),
                s.getCourse().getCreator().getName()));
        cancellations.forEach(c -> creatorNames.put(
                c.getSaleRecord().getCourse().getCreator().getId(),
                c.getSaleRecord().getCourse().getCreator().getName()));

        List<CreatorSettlementSummary> settlements = allCreatorIds.stream()
                .map(creatorId -> {
                    List<SaleRecord> creatorSales = salesByCreator.getOrDefault(creatorId, List.of());
                    List<CancellationRecord> creatorCancellations = cancellationsByCreator.getOrDefault(creatorId, List.of());

                    BigDecimal totalSales = sum(creatorSales.stream().map(SaleRecord::getAmount).toList());
                    BigDecimal totalRefunds = sum(creatorCancellations.stream().map(CancellationRecord::getRefundAmount).toList());
                    BigDecimal netSales = totalSales.subtract(totalRefunds);
                    BigDecimal platformFee = fee(netSales);
                    BigDecimal settlementAmount = netSales.subtract(platformFee);

                    return new CreatorSettlementSummary(
                            creatorId,
                            creatorNames.get(creatorId),
                            totalSales,
                            totalRefunds,
                            netSales,
                            platformFee,
                            settlementAmount,
                            creatorSales.size(),
                            creatorCancellations.size()
                    );
                })
                .sorted(Comparator.comparing(CreatorSettlementSummary::creatorId))
                .toList();

        BigDecimal totalSettlementAmount = sum(settlements.stream()
                .map(CreatorSettlementSummary::settlementAmount).toList());

        return new SettlementSummaryResponse(from.toString(), to.toString(), settlements, totalSettlementAmount);
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal fee(BigDecimal netSales) {
        return netSales.multiply(FEE_RATE).setScale(0, RoundingMode.DOWN);
    }
}
