package com.acme.wo;

import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutDefinitions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;

@Service
public class YamlWorkoutService implements WorkoutService {

    private final WorkoutDefinitions workoutDefinitions;
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public YamlWorkoutService() throws IOException {

        ClassPathResource r = new  ClassPathResource("workouts/workouts.yaml");

        try (var inputStream = r.getInputStream()) {

            workoutDefinitions =
                    objectMapper.readValue(
                            inputStream,
                            WorkoutDefinitions.class
                    );
        }
    }

    @Override
    public List<Workout> findAll() {
        return workoutDefinitions.workouts();
    }

    @Override
    public Workout findById(String workoutId) {
        return workoutDefinitions.workouts().stream()
                .filter(w->w.name().equalsIgnoreCase(workoutId))
                .findFirst()
                .orElse(null);
    }
}
