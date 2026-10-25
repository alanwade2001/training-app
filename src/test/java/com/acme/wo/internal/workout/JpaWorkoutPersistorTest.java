package com.acme.wo.internal.workout;

import com.acme.wo.model.ExerciseUnit;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({JpaWorkoutPersistor.class})
class JpaWorkoutPersistorTest {

    @Autowired
    JpaWorkoutPersistor jpaWorkoutPersistor;

    @Test
    void persist() {
        Workout w = new Workout(
                null,
                "workout 1",
                DayOfWeek.MONDAY,
                List.of(
                        new WorkoutExercise(1, "Pogos", 3, 20, 0, ExerciseUnit.KG, 90)
                )
        );

        var persisted = jpaWorkoutPersistor.persist(w);

        Assertions.assertThat(persisted).isNotNull();
        Assertions.assertThat(persisted.id()).isPositive();

        // find the workout
        var workouts = jpaWorkoutPersistor.getWorkouts();
        Assertions.assertThat(workouts).hasSize(1);

        var found = workouts.stream().filter(workout->workout.name().equals(w.name())).findFirst().orElseThrow();

        Assertions.assertThat(found.exercises()).hasSize(1);
    }
}