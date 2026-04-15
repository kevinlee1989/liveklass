package com.example.settlement;

import com.example.settlement.dto.MonthlySettlementResponse;
import com.example.settlement.dto.SettlementStatusRequest;
import com.example.settlement.dto.SettlementSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/creators/{creatorId}")
    public MonthlySettlementResponse getCreatorSettlement(
            @PathVariable String creatorId,
            @RequestParam String month
    ) {
        return settlementService.calculate(creatorId, YearMonth.parse(month));
    }

    @PatchMapping("/creators/{creatorId}")
    public MonthlySettlementResponse updateStatus(
            @PathVariable String creatorId,
            @RequestParam String month,
            @Valid @RequestBody SettlementStatusRequest request
    ) {
        YearMonth yearMonth = YearMonth.parse(month);
        return switch (request.status()) {
            case CONFIRMED -> settlementService.confirm(creatorId, yearMonth);
            case PAID      -> settlementService.pay(creatorId, yearMonth);
            case PENDING   -> throw new IllegalArgumentException("PENDING으로 직접 전환할 수 없습니다.");
        };
    }

    @GetMapping("/summary")
    public SettlementSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return settlementService.summarize(from, to);
    }
}
