package com.acme.wo.model.upload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.util.List;

public record WeekUpload(
        @Positive
        int number,

        @NotEmpty
        List<@Valid WorkoutUpload> workouts

) {

}
