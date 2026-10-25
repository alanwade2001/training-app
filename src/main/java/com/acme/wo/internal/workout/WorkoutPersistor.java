package com.acme.wo.internal.workout;

import com.acme.wo.model.Workout;

import java.util.List;

public interface WorkoutPersistor {
    List<Workout> persist(List<Workout> workouts);

    Workout persist(Workout w);
}
