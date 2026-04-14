package com.example.common;

public record ErrorResponse(
        int status,
        String message
) {
}
