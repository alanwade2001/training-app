package com.acme.wo.service;

import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.Workout;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class WorkoutService {

    private final List<WorkoutSource> sources;
    private final YamlUploadService uploadService;
    private final WorkoutPersistor workoutPersistor;

    public WorkoutService(List<WorkoutSource> sources, YamlUploadService uploadService, WorkoutPersistor workoutPersistor) {
        this.sources = sources;
        this.uploadService = uploadService;
        this.workoutPersistor = workoutPersistor;
    }

    public List<Workout> findAll() {
        var workouts = new ArrayList<Workout>();
        sources.forEach(s->workouts.addAll(s.getWorkouts()));
        workouts.sort(Comparator.comparingLong(Workout::id));
        return workouts;
    }

    public Workout findById(Long workoutId) {
        var workouts = findAll();
        return workouts.stream().filter(w->workoutId.equals(w.id())).findFirst().orElse(null);
    }

    public Workout findByName(String name) {
        var workouts = findAll();
        return workouts.stream().filter(w->name.equals(w.name())).findFirst().orElse(null);
    }

    public List<Workout> persist(InputStreamSource source) {
        var workout = uploadService.importWorkout(source);
        return workoutPersistor.persist(workout.workouts());
    }
}
