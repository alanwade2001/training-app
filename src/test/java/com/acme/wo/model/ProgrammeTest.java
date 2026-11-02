package com.acme.wo.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProgrammeTest {

    private final LocalDate startDate = LocalDate.of(2026, 11, 2);

    @Test
    void shouldBeInWeekOneOnStartDate() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.currentWeek(startDate))
                .isEqualTo(1);
    }

    @Test
    void shouldRemainInWeekOneForFirstSevenDays() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.currentWeek(startDate.plusDays(6)))
                .isEqualTo(1);
    }

    @Test
    void shouldMoveToWeekTwoAfterSevenDays() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.currentWeek(startDate.plusDays(7)))
                .isEqualTo(2);
    }

    @Test
    void shouldBeInFinalWeek() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.currentWeek(startDate.plusDays(27)))
                .isEqualTo(4);

        assertThat(programme.isCompleted(startDate.plusDays(27)))
                .isFalse();
    }

    @Test
    void shouldBeCompletedAfterFinalWeek() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.isCompleted(startDate.plusDays(28)))
                .isTrue();
    }

    @Test
    void shouldReturnWeekZeroBeforeProgrammeStarts() {
        Programme programme = programmeWithFourWeeks();

        assertThat(programme.currentWeek(startDate.minusDays(1)))
                .isEqualTo(0);
    }

    private Programme programmeWithFourWeeks() {
        return new Programme(1L, "400m Programme", startDate, List.of(
                        new Week(1, List.of()),
                        new Week(2, List.of()),
                        new Week(3, List.of()),
                        new Week(4, List.of())
                )
        );
    }
}