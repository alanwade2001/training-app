package com.acme.wo.internal.workout;

import com.acme.wo.model.ProgrammeLocation;
import com.acme.wo.model.Workout;

import java.util.List;
import java.util.Optional;

public interface WorkoutPersistor {
    List<Workout> persist(List<Workout> workouts);

    Workout persist(Workout w);

    Optional<ProgrammeLocation> findProgrammeLocation(Long workoutId);

    void deleteWorkout(Long workoutId);
}
