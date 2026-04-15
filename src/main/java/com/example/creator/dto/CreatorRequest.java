package com.example.creator.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatorRequest(
        @NotBlank String id,
        @NotBlank String name
) {
}
