package com.example.course.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseTitleUpdateRequest(
        @NotBlank String title
) {
}