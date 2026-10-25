package com.acme.wo.model.upload;

import com.acme.wo.model.Workout;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkoutDefinitionsUpload(
        @NotEmpty
        List<WorkoutUpload> workouts
) {
}
