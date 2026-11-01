package com.acme.wo.internal.bootstrap;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;
import com.acme.wo.service.YamlUploadService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class WorkoutBootstrapImporter {

    private final BootstrapResourceLoader resourceLoader;
    private final YamlUploadService yamlUploadService;
    private final WorkoutPersistor workoutPersistor;

    public WorkoutBootstrapImporter(BootstrapResourceLoader resourceLoader, YamlUploadService yamlUploadService, WorkoutPersistor workoutPersistor) {
        this.resourceLoader = resourceLoader;
        this.yamlUploadService = yamlUploadService;
        this.workoutPersistor = workoutPersistor;
    }


    public void importDirectory(String location) {
        resourceLoader.resources(location)
                .forEach(this::importWorkout);
    }

    private void importWorkout(Resource resource) {

        var definitions =
                yamlUploadService.importWorkout(resource);

        workoutPersistor.persist(definitions.workouts());
    }
}