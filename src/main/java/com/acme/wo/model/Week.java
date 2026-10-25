package com.acme.wo.model;

import java.util.List;

public record Week(
        int number,
        List<Workout> workouts
) {
}
