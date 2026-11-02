package com.acme.wo.service;

import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.ProgrammeLocation;
import com.acme.wo.model.Workout;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class WorkoutService {

    private final YamlUploadService uploadService;
    private final WorkoutPersistor workoutPersistor;

    public WorkoutService(YamlUploadService uploadService, WorkoutPersistor workoutPersistor) {
        this.uploadService = uploadService;
        this.workoutPersistor = workoutPersistor;
    }


    public List<Workout> findAll() {
        var workouts = workoutPersistor.getWorkouts();
        workouts.sort(Comparator.comparingLong(Workout::id));
        return workouts;
    }

    public List<Workout> findAllStandaloneWorkouts() {
        return workoutPersistor.getStandaloneWorkouts();
    }

    public Optional<Workout> findById(Long workoutId) {
        return workoutPersistor.findById(workoutId);
    }

    public Optional<Workout> findByName(String name) {
        return workoutPersistor.findByName(name);
    }

    public List<Workout> persist(InputStreamSource source) {
        var workout = uploadService.importWorkout(source);
        return workoutPersistor.persist(workout.workouts());
    }

    public Optional<ProgrammeLocation> findProgrammeLocation(
            Long workoutId) {

        return workoutPersistor.findProgrammeLocation(workoutId);
    }

    public void deleteStandalone(Long workoutId) {

        if (findProgrammeLocation(workoutId).isPresent()) {
            throw new IllegalStateException(
                    "A programme workout cannot be deleted independently"
            );
        }

        workoutPersistor.deleteWorkout(workoutId);
    }
}
