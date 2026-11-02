package com.acme.wo.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record Programme(
        Long id,
        String name,
        LocalDate startDate,
        List<Week> weeks
) {

    public int currentWeek(LocalDate date) {
        long days = ChronoUnit.DAYS.between(startDate, date);

        if (days < 0) {
            return 0;
        }

        return Math.toIntExact(days / 7) + 1;
    }

    public boolean isCompleted(LocalDate date) {
        return currentWeek(date) > weeks.size();
    }
}
