package com.acme.wo.model;

public record ValidationError(
        String path,
        String message
) {
}
