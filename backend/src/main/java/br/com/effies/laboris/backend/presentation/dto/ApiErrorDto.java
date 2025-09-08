package br.com.effies.laboris.backend.presentation.dto;

import java.time.Instant;

public record ApiErrorDto (
    Instant timestamp,
    Integer status,
    String error,
    String message,
    String path
){}
