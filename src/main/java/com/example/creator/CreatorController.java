package com.example.creator;

import com.example.creator.dto.CreatorRequest;
import com.example.creator.dto.CreatorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.creator.dto.CreatorResponse;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final CreatorService creatorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody CreatorRequest request) {
        String id = creatorService.register(request);
        return Map.of("id", id);
    }

    @GetMapping
    public List<CreatorResponse> findAllNames() {
        return creatorService.findAll();
    }
    
}
