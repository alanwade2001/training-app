package com.acme.wo.internal.programme;

import com.acme.wo.model.Workout;

public record WorkoutProgress(
        Workout workout,
        long sessionCount,
        long completedSessionCount) {
    public boolean done() { return completedSessionCount > 0;}
}
