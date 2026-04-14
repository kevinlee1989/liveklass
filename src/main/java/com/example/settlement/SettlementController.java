package com.example.settlement;

import com.example.settlement.dto.MonthlySettlementResponse;
import com.example.settlement.dto.SettlementSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/summary")
    public SettlementSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return settlementService.summarize(from, to);
    }
}
