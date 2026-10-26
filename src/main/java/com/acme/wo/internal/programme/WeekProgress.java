package com.acme.wo.internal.programme;

import com.acme.wo.model.Week;

public record WeekProgress(
        Week week,
        long completedWorkouts
) {

    public int totalWorkouts() {
        return week.workouts().size();
    }

    public boolean done() {
        return completedWorkouts == totalWorkouts()
                && totalWorkouts() > 0;
    }
}
