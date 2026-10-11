package com.acme.wo;

import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class HardcodedWorkoutService implements WorkoutService {

    private final List<Workout> workouts = List.of(

            new Workout(
                    "heavy-force",
                    "Heavy Force",
                    List.of(
                            new WorkoutExercise(
                                    1,
                                    "Dead Lift",
                                    2,
                                    2,
                                    120
                            ),
                            new WorkoutExercise(
                                    2,
                                    "Bulgarian Split Squat",
                                    3,
                                    6,
                                    90
                            ),
                            new WorkoutExercise(
                                    3,
                                    "Romanian Deadlift",
                                    3,
                                    6,
                                    120
                            )
                    )
            ),

            new Workout(
                    "robustness",
                    "Robustness",
                    List.of(
                            new WorkoutExercise(
                                    1,
                                    "25 s Copenhagen Plank",
                                    1,
                                    25,
                                    60
                            ),
                            new WorkoutExercise(
                                    2,
                                    "Single-leg Calf Raise",
                                    3,
                                    12,
                                    60
                            )
                    )
            )
    );

    @Override
    public List<Workout> findAll() {
        return workouts;
    }

    @Override
    public Workout findById(String workoutId) {
        return workouts.stream()
                .filter(workout ->
                        workout.id().equals(workoutId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Workout not found: "
                                        + workoutId
                        )
                );
    }
}
