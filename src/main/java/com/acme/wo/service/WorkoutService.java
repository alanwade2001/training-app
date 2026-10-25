package com.acme.wo.service;

import com.acme.wo.model.Workout;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class WorkoutService {

    private final List<WorkoutSource> sources;

    public WorkoutService(List<WorkoutSource> sources) {
        this.sources = sources;
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
}
