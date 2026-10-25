package com.acme.wo.service;

import com.acme.wo.model.*;
import com.acme.wo.model.upload.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class YamlUploadService {

    private final ObjectMapper yamlMapper;
    private final Validator validator;

    public YamlUploadService(Validator validator) {

        this.validator = validator;

        this.yamlMapper =
                new ObjectMapper(new YAMLFactory());

        this.yamlMapper.findAndRegisterModules();
    }

    public WorkoutDefinitions importWorkout(
            InputStreamSource source) {

        try (var stream = source.getInputStream()){
            return importWorkout(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkoutDefinitions importWorkout(
            InputStream stream) {

        WorkoutDefinitionsUpload upload =
                parseWorkout(stream);

        validateWorkout(upload);

        return maptoWorkoutDefinitions(upload);
    }

    public WorkoutDefinitionsUpload parseWorkout(
            InputStream input) {

        try {

            return yamlMapper.readValue(
                    input,
                    WorkoutDefinitionsUpload.class
            );

        } catch (JsonProcessingException exception) {

            throw new WorkoutUploadException(
                    "The workout YAML is invalid: "
                            + exception.getOriginalMessage(),
                    exception);
        } catch (IOException exception) {

            throw new WorkoutUploadException(
                    "Cannot read the file: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private void validateWorkout(WorkoutDefinitionsUpload upload) {

        Set<ConstraintViolation<WorkoutDefinitionsUpload>> violations =
                validator.validate(upload);

        if (violations.isEmpty()) {
            return;
        }

        List<String> errors = violations.stream()
                .map(violation ->
                        violation.getPropertyPath()
                                + " "
                                + violation.getMessage()
                )
                .sorted()
                .toList();

        throw new WorkoutUploadException(
                String.format("The workout is invalid: %s", errors)
        );
    }

    private WorkoutDefinitions maptoWorkoutDefinitions(WorkoutDefinitionsUpload upload) {
        var definitions = IntStream.range(0, upload.workouts().size())
                .mapToObj(i->mapToWorkout((long)i, upload.workouts().get(i)))
                .toList();

        return new WorkoutDefinitions(definitions);
    }

    private Workout mapToWorkout(Long id, WorkoutUpload upload) {

        List<WorkoutExercise> exercises =
                IntStream.range(0, upload.exercises().size())
                        .mapToObj(index ->
                                mapToWorkoutExercise(
                                        upload.exercises().get(index),
                                        index + 1
                                )
                        )
                        .toList();

        return new Workout(
                id,
                upload.name(),
                upload.plannedDay(),
                exercises
        );
    }

    private WorkoutExercise mapToWorkoutExercise(
            WorkoutExerciseUpload upload,
            int sequence) {

        return new WorkoutExercise(
                sequence,
                upload.name(),
                upload.sets(),
                upload.reps(),
                upload.quantity(),
                upload.units(),
                upload.recoveryInSecs()
        );
    }


    public Programme importProgramme(
            InputStreamSource source) {
        try (var stream = source.getInputStream()){
            return importProgramme(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Programme importProgramme(
            InputStream stream) {

        ProgrammeUpload upload =
                parse(stream, ProgrammeUpload.class);

        validate(upload);

        return mapToProgramme(upload);
    }

    public <T> T parse(
            InputStream input,
            Class<T> clazz) {

        try {

            return yamlMapper.readValue(
                    input,
                    clazz
            );

        } catch (JsonProcessingException exception) {

            throw new WorkoutUploadException(
                    "The workout YAML is invalid: "
                            + exception.getOriginalMessage(),
                    exception);
        } catch (IOException exception) {

            throw new WorkoutUploadException(
                    "Cannot read the file: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private void validate(Object upload) {

        var violations =
                validator.validate(upload);

        if (violations.isEmpty()) {
            return;
        }

        List<String> errors = violations.stream()
                .map(violation ->
                        violation.getPropertyPath()
                                + " "
                                + violation.getMessage()
                )
                .sorted()
                .toList();

        throw new WorkoutUploadException(
                String.format("The workout is invalid: %s", errors)
        );
    }

    private Programme mapToProgramme(ProgrammeUpload upload) {

        var weeks = upload.weeks().stream().map(this::mapToWeek).toList();

        return new Programme(
                null,
                upload.name(),
                weeks
        );
    }

    private Week mapToWeek(WeekUpload upload) {

        var workouts = IntStream.range(0, upload.workouts().size())
                .mapToObj(i->mapToWorkout((long)i, upload.workouts().get(i)))
                .toList();

        return new Week(
                upload.number(),
          workouts
        );
    }

}