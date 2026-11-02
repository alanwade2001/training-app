package com.acme.wo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Programme(
        Long id,
        String name,
        LocalDate startDate,
        List<Week> weeks
) {
}
