package com.acme.wo.model.upload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkoutDefinitionsUpload(
        @NotEmpty
        List<@Valid WorkoutUpload> workouts
) {
}
