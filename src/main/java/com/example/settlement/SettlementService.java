package com.example.settlement;

import com.example.cancellation.CancellationRecord;
import com.example.cancellation.CancellationRecordMapper;
import com.example.course.Course;
import com.example.course.CourseMapper;
import com.example.creator.CreatorMapper;
import com.example.sale.SaleRecord;
import com.example.sale.SaleRecordMapper;
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

    private final SaleRecordMapper saleRecordMapper;
    private final CancellationRecordMapper cancellationRecordMapper;
    private final SettlementMapper settlementMapper;
    private final CreatorMapper creatorMapper;
    private final CourseMapper courseMapper;

    // ── GET: 상태 확인 + 금액 조회 ────────────────────────────────

    public MonthlySettlementResponse calculate(String creatorId, YearMonth month) {
        return settlementMapper.findByCreatorIdAndMonth(creatorId, month.toString())
                .map(MonthlySettlementResponse::from)
                .orElseGet(() -> computePending(creatorId, month));
    }

    // ── PATCH: 상태 전환 ──────────────────────────────────────────

    @Transactional
    public MonthlySettlementResponse confirm(String creatorId, YearMonth month) {
        settlementMapper.findByCreatorIdAndMonth(creatorId, month.toString())
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

        settlementMapper.insert(settlement);
        return MonthlySettlementResponse.from(settlement);
    }

    @Transactional
    public MonthlySettlementResponse pay(String creatorId, YearMonth month) {
        Settlement settlement = settlementMapper
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
        settlementMapper.update(settlement);
        return MonthlySettlementResponse.from(settlement);
    }

    // ── 운영자 기간별 집계 ────────────────────────────────────────

    public SettlementSummaryResponse summarize(LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = from.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay(KST).toOffsetDateTime();

        List<SaleRecord> sales = saleRecordMapper.findByPaidAtBetween(fromDt, toDt);
        List<CancellationRecord> cancellations = cancellationRecordMapper.findByCanceledAtBetween(fromDt, toDt);

        // 모든 courseId 수집 (sales + cancellations의 sale record)
        Set<String> courseIds = new HashSet<>();
        sales.forEach(s -> courseIds.add(s.getCourseId()));

        // cancellation의 sale record를 조회해 courseId 확보
        Map<String, String> cancellationSaleToCoursId = new HashMap<>();
        cancellations.forEach(c -> {
            String srId = c.getSaleRecordId();
            saleRecordMapper.findById(srId).ifPresent(sr -> {
                courseIds.add(sr.getCourseId());
                cancellationSaleToCoursId.put(srId, sr.getCourseId());
            });
        });

        Map<String, String> courseToCreator = courseIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> courseMapper.findById(id).map(Course::getCreatorId).orElse(id)
                ));

        Map<String, List<SaleRecord>> salesByCreator = sales.stream()
                .collect(Collectors.groupingBy(s -> courseToCreator.get(s.getCourseId())));

        Map<String, List<CancellationRecord>> cancellationsByCreator = cancellations.stream()
                .collect(Collectors.groupingBy(c -> courseToCreator.get(cancellationSaleToCoursId.get(c.getSaleRecordId()))));

        Set<String> allCreatorIds = new HashSet<>();
        allCreatorIds.addAll(salesByCreator.keySet());
        allCreatorIds.addAll(cancellationsByCreator.keySet());

        Map<String, String> creatorNames = allCreatorIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> creatorMapper.findById(id).map(c -> c.getName()).orElse(id)
                ));

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

        List<SaleRecord> sales = saleRecordMapper.findByCreatorAndPaidAtBetween(creatorId, from, to);
        List<CancellationRecord> cancellations = cancellationRecordMapper.findByCreatorAndCanceledAtBetween(creatorId, from, to);

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
