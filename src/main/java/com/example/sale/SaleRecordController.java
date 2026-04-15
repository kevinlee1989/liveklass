package com.example.sale;

import com.example.sale.dto.SaleRecordRequest;
import com.example.sale.dto.SaleRecordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sale-records")
@RequiredArgsConstructor
public class SaleRecordController {

    private final SaleRecordService saleRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody SaleRecordRequest request) {
        String id = saleRecordService.register(request);
        return Map.of("id", id);
    }

    @GetMapping
    public List<SaleRecordResponse> getList(
            @RequestParam String creatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return saleRecordService.getList(creatorId, from, to);
    }
}
