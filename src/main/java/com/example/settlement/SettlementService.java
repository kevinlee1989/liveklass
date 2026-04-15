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
    private final SettlementRepository settlementRepository;

    // ── GET: 상태 확인 + 금액 조회 ────────────────────────────────

    public MonthlySettlementResponse calculate(String creatorId, YearMonth month) {
        // CONFIRMED / PAID 이면 스냅샷 반환
        return settlementRepository.findByCreatorIdAndMonth(creatorId, month.toString())
                .map(MonthlySettlementResponse::from)
                .orElseGet(() -> computePending(creatorId, month));
    }

    // ── PATCH: 상태 전환 ──────────────────────────────────────────

    @Transactional
    public MonthlySettlementResponse confirm(String creatorId, YearMonth month) {
        settlementRepository.findByCreatorIdAndMonth(creatorId, month.toString())
                .ifPresent(existing -> {
                    if (existing.getStatus() == SettlementStatus.CONFIRMED) {
                        throw new IllegalArgumentException("이미 확정된 정산입니다: " + creatorId + " " + month);
                    }
                    if (existing.getStatus() == SettlementStatus.PAID) {
                        throw new IllegalArgumentException("이미 지급 완료된 정산입니다: " + creatorId + " " + month);
                    }
                });

        MonthlySettlementResponse calc = computePending(creatorId, month);

        Settlement settlement = Settlement.confirm(
                creatorId, month.toString(),
                calc.totalSales(), calc.totalRefunds(), calc.netSales(),
                calc.platformFee(), calc.settlementAmount(),
                calc.saleCount(), calc.cancellationCount()
        );

        return MonthlySettlementResponse.from(settlementRepository.save(settlement));
    }

    @Transactional
    public MonthlySettlementResponse pay(String creatorId, YearMonth month) {
        Settlement settlement = settlementRepository
                .findByCreatorIdAndMonth(creatorId, month.toString())
                .orElseThrow(() -> new IllegalArgumentException(
                        "확정되지 않은 정산입니다. 먼저 CONFIRMED 처리가 필요합니다: " + creatorId + " " + month));

        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new IllegalArgumentException("이미 지급 완료된 정산입니다: " + creatorId + " " + month);
        }
        if (settlement.getStatus() != SettlementStatus.CONFIRMED) {
            throw new IllegalArgumentException("CONFIRMED 상태에서만 PAID로 전환할 수 있습니다.");
        }

        settlement.markAsPaid();
        return MonthlySettlementResponse.from(settlement);
    }

    // ── 운영자 기간별 집계 ────────────────────────────────────────

    public SettlementSummaryResponse summarize(LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = from.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay(KST).toOffsetDateTime();

        List<SaleRecord> sales = saleRecordRepository.findByPaidAtBetween(fromDt, toDt);
        List<CancellationRecord> cancellations = cancellationRecordRepository.findByCanceledAtBetween(fromDt, toDt);

        Map<String, List<SaleRecord>> salesByCreator = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getCourse().getCreator().getId()));

        Map<String, List<CancellationRecord>> cancellationsByCreator = cancellations.stream()
                .collect(Collectors.groupingBy(c -> c.getSaleRecord().getCourse().getCreator().getId()));

        Set<String> allCreatorIds = new HashSet<>();
        allCreatorIds.addAll(salesByCreator.keySet());
        allCreatorIds.addAll(cancellationsByCreator.keySet());

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
                            totalSales, totalRefunds, netSales, platformFee, settlementAmount,
                            creatorSales.size(), creatorCancellations.size()
                    );
                })
                .sorted(Comparator.comparing(CreatorSettlementSummary::creatorId))
                .toList();

        BigDecimal totalSettlementAmount = sum(settlements.stream()
                .map(CreatorSettlementSummary::settlementAmount).toList());

        return new SettlementSummaryResponse(from.toString(), to.toString(), settlements, totalSettlementAmount);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────

    private MonthlySettlementResponse computePending(String creatorId, YearMonth month) {
        OffsetDateTime from = month.atDay(1).atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime to = month.plusMonths(1).atDay(1).atStartOfDay(KST).toOffsetDateTime();

        List<SaleRecord> sales = saleRecordRepository.findByCreatorAndPaidAtBetween(creatorId, from, to);
        List<CancellationRecord> cancellations = cancellationRecordRepository.findByCreatorAndCanceledAtBetween(creatorId, from, to);

        BigDecimal totalSales = sum(sales.stream().map(SaleRecord::getAmount).toList());
        BigDecimal totalRefunds = sum(cancellations.stream().map(CancellationRecord::getRefundAmount).toList());
        BigDecimal netSales = totalSales.subtract(totalRefunds);
        BigDecimal platformFee = fee(netSales);
        BigDecimal settlementAmount = netSales.subtract(platformFee);

        return MonthlySettlementResponse.pending(
                creatorId, month.toString(),
                totalSales, totalRefunds, netSales, platformFee, settlementAmount,
                sales.size(), cancellations.size()
        );
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal fee(BigDecimal netSales) {
        return netSales.multiply(FEE_RATE).setScale(0, RoundingMode.DOWN);
    }
}
