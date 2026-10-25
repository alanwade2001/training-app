package com.acme.wo.internal.workout;

import com.acme.wo.model.ExerciseUnit;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import com.acme.wo.service.WorkoutSource;

import java.time.DayOfWeek;
import java.util.List;

//@Service
public class HardcodedWorkoutSource implements WorkoutSource {

    private final List<Workout> workouts = List.of(

            new Workout(
                    1L,
                    "Heavy Force",
                    DayOfWeek.MONDAY,
                    List.of(
                            new WorkoutExercise(
                                    1,
                                    "Dead Lift",
                                    2,
                                    2,
                                    77,
                                    ExerciseUnit.KG,
                                    120
                            ),
                            new WorkoutExercise(
                                    2,
                                    "Bulgarian Split Squat",
                                    3,
                                    6,
                                    10,
                                    ExerciseUnit.KG,
                                    90
                            ),
                            new WorkoutExercise(
                                    3,
                                    "Romanian Deadlift",
                                    3,
                                    6,
                                    57,
                                    ExerciseUnit.KG,
                                    120
                            )
                    )
            ),

            new Workout(
                    2L,
                    "Robustness",
                    DayOfWeek.WEDNESDAY,
                    List.of(
                            new WorkoutExercise(
                                    1,
                                    "25 s Copenhagen Plank",
                                    1,
                                    1,
                                    25,
                                    ExerciseUnit.SECONDS,
                                    60
                            ),
                            new WorkoutExercise(
                                    2,
                                    "Single-leg Calf Raise",
                                    3,
                                    12,
                                    0,
                                    ExerciseUnit.KG,
                                    60
                            )
                    )
            )
    );

    public List<Workout> getWorkouts() {
        return workouts;
    }


}
