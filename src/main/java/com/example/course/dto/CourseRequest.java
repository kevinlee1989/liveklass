package com.example.course.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank String id,
        @NotBlank String creatorId,
        @NotBlank String title
) {
}
