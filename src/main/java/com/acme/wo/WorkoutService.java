package com.acme.wo;

import com.acme.wo.model.Workout;

import java.util.List;

public interface WorkoutService {
    List<Workout> findAll();

    Workout findById(String workoutId);
}
