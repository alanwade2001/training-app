package com.acme.wo.model.upload;

import com.acme.wo.model.ExerciseUnit;
import jakarta.validation.constraints.*;

public record WorkoutExerciseUpload(
        @NotBlank String name,
        @Positive int sets,
        @Positive int reps,
        @PositiveOrZero int quantity,
        @NotNull ExerciseUnit units,
        @Min(15) int recoveryInSecs
) {
}
