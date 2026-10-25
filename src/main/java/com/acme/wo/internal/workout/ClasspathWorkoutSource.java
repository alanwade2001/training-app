package com.acme.wo.internal.workout;

import com.acme.wo.internal.utils.ClassPathResourceLoader;
import com.acme.wo.service.YamlUploadService;
import com.acme.wo.model.Workout;
import com.acme.wo.service.WorkoutSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ClasspathWorkoutSource implements WorkoutSource {

    private final YamlUploadService uploadService;
    public ClasspathWorkoutSource(YamlUploadService uploadService) throws IOException {
        this.uploadService = uploadService;
    }

    @Override
    public List<Workout> getWorkouts() {
        var loader = new ClassPathResourceLoader();

        var resources = loader.load("classpath:workouts/*.yml");

        var workouts = new ArrayList<Workout>();

        var nextid = 100L;

        for (Resource r : resources) {
            var workoutDefinitions = uploadService.importWorkout(r);
            var workoutList = workoutDefinitions.workouts();

            for (Workout w : workoutList) {
                nextid++;
                var newW =  new Workout(nextid, w.name(), w.dayOfWeek(), w.exercises());
                workouts.add(newW);
            }
        }

        return workouts;
    }


}
