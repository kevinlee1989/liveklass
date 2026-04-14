package com.example.cancellation;

import com.example.cancellation.dto.CancellationRecordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cancellation-records")
@RequiredArgsConstructor
public class CancellationRecordController {

    private final CancellationRecordService cancellationRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> register(@RequestBody CancellationRecordRequest request) {
        Long id = cancellationRecordService.register(request);
        return Map.of("id", id);
    }
}
