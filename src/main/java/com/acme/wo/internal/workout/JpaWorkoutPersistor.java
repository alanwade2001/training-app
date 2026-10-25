package com.acme.wo.internal.workout;

import com.acme.wo.internal.data.WorkoutEntity;
import com.acme.wo.internal.data.WorkoutExerciseEntity;
import com.acme.wo.internal.data.WorkoutExerciseRepository;
import com.acme.wo.internal.data.WorkoutRepository;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import com.acme.wo.service.WorkoutSource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Component
public class JpaWorkoutPersistor implements WorkoutSource, WorkoutPersistor {

    private final WorkoutRepository  workoutRepository;

    public JpaWorkoutPersistor(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }


    @Override
    public List<Workout> persist(List<Workout> workouts) {
        return workouts.stream().map(this::persist).toList();
    }

    @Override
    public Workout persist(Workout w) {
        var workoutEntity = WorkoutMapper.toEntity(w);

        var savedWorkoutEntity = workoutRepository.save(workoutEntity);

        return new Workout(savedWorkoutEntity.getId()
                , savedWorkoutEntity.getName()
                , savedWorkoutEntity.getPlannedDay()
                , w.exercises());

    }

    @Override
    public List<Workout> getWorkouts() {
        return workoutRepository.findAll().stream().map(WorkoutMapper::fromEntity).toList();
    }
}
