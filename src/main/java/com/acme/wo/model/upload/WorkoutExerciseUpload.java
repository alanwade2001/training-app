package com.acme.wo.model.upload;

import com.acme.wo.model.ExerciseUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record WorkoutExerciseUpload(
        @NotBlank String name,
        @Positive int sets,
        @Positive int reps,
        @PositiveOrZero int quantity,
        @NotNull ExerciseUnit units,
        @PositiveOrZero int recoveryInSecs
) {
}
