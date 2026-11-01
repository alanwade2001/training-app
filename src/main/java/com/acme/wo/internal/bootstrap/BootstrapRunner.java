package com.acme.wo.internal.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BootstrapRunner implements ApplicationRunner {

    private final BootstrapProperties properties;
    private final ProgrammeBootstrapImporter programmeImporter;
    private final WorkoutBootstrapImporter workoutImporter;

    public BootstrapRunner(BootstrapProperties properties, ProgrammeBootstrapImporter programmeImporter, WorkoutBootstrapImporter workoutImporter) {
        this.properties = properties;
        this.programmeImporter = programmeImporter;
        this.workoutImporter = workoutImporter;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (!properties.enabled()) {
            log.info("Bootstrap data loading is disabled");
            return;
        }

        log.info("Bootstrap data loading enabled");
        log.info(
                "Programme bootstrap location: {}",
                properties.programmes()
        );
        log.info(
                "Workout bootstrap location: {}",
                properties.workouts()
        );

        programmeImporter.importDirectory(
                properties.programmes()
        );

        workoutImporter.importDirectory(
                properties.workouts()
        );

        log.info("Bootstrap data loading completed");
    }
}
