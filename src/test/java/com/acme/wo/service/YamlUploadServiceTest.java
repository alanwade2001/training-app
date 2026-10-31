package com.acme.wo.service;

import com.acme.wo.model.ExerciseUnit;
import com.acme.wo.model.Programme;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlUploadServiceProgrammeTest {

    private YamlUploadService service;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        Validator validator = factory.getValidator();

        service = new YamlUploadService(validator);
    }

    @Test
    void importsValidProgramme() {
        String yaml = """
                name: 400m Programme
                weeks:
                  - number: 1
                    workouts:
                      - name: Speed
                        plannedDay: TUESDAY
                        exercises:
                          - name: 200m
                            sets: 2
                            reps: 3
                            quantity: 200
                            units: METERS
                            recoveryInSecs: 120

                  - number: 2
                    workouts:
                      - name: Speed Endurance
                        plannedDay: THURSDAY
                        exercises:
                          - name: 400m
                            sets: 3
                            reps: 1
                            quantity: 400
                            units: METERS
                            recoveryInSecs: 180
                """;

        Programme programme =
                service.importProgramme(stream(yaml));

        assertEquals("400m Programme", programme.name());
        assertEquals(2, programme.weeks().size());

        var week1 = programme.weeks().getFirst();

        assertEquals(1, week1.number());
        assertEquals(1, week1.workouts().size());

        var workout1 = week1.workouts().getFirst();

        assertEquals("Speed", workout1.name());
        assertEquals("TUESDAY", workout1.dayOfWeek().name());
        assertEquals(1, workout1.exercises().size());

        var exercise1 = workout1.exercises().getFirst();

        assertEquals(1, exercise1.sequence());
        assertEquals("200m", exercise1.name());
        assertEquals(2, exercise1.sets());
        assertEquals(3, exercise1.reps());
        assertEquals(200, exercise1.quantity());
        assertEquals(ExerciseUnit.METERS, exercise1.units());
        assertEquals(120, exercise1.recoveryInSecs());

        var week2 = programme.weeks().get(1);

        assertEquals(2, week2.number());

        var workout2 = week2.workouts().getFirst();

        assertEquals("Speed Endurance", workout2.name());
        assertEquals(
                "THURSDAY",
                workout2.dayOfWeek().name()
        );

        var exercise2 = workout2.exercises().getFirst();

        assertEquals("400m", exercise2.name());
        assertEquals(180, exercise2.recoveryInSecs());
    }

    @Test
    void preservesOrdering() {
        String yaml = """
                name: Ordering Test
                weeks:
                  - number: 3
                    workouts:
                      - name: Workout C
                        plannedDay: MONDAY
                        exercises:
                          - name: Exercise C
                            sets: 1
                            reps: 1
                            quantity: 300
                            units: METERS
                            recoveryInSecs: 60

                      - name: Workout A
                        plannedDay: TUESDAY
                        exercises:
                          - name: Exercise B
                            sets: 1
                            reps: 1
                            quantity: 200
                            units: METERS
                            recoveryInSecs: 60

                          - name: Exercise A
                            sets: 1
                            reps: 1
                            quantity: 100
                            units: METERS
                            recoveryInSecs: 60

                  - number: 1
                    workouts:
                      - name: Workout B
                        plannedDay: THURSDAY
                        exercises:
                          - name: Exercise D
                            sets: 1
                            reps: 1
                            quantity: 400
                            units: METERS
                            recoveryInSecs: 60
                """;

        Programme programme =
                service.importProgramme(stream(yaml));

        // Week order
        assertEquals(3, programme.weeks().get(0).number());
        assertEquals(1, programme.weeks().get(1).number());

        var firstWeek = programme.weeks().get(0);

        // Workout order
        assertEquals(
                "Workout C",
                firstWeek.workouts().get(0).name()
        );

        assertEquals(
                "Workout A",
                firstWeek.workouts().get(1).name()
        );

        var workoutA = firstWeek.workouts().get(1);

        // Exercise order
        assertEquals(
                "Exercise B",
                workoutA.exercises().getFirst().name()
        );

        assertEquals(
                "Exercise A",
                workoutA.exercises().get(1).name()
        );

        // YamlUploadService generates sequence numbers
        // from the exercise list position.
        assertEquals(
                1,
                workoutA.exercises().getFirst().sequence()
        );

        assertEquals(
                2,
                workoutA.exercises().get(1).sequence()
        );
    }

    @Test
    void rejectsMalformedYaml() {
        String yaml = """
                name: Broken Programme
                weeks:
                  - number: 1
                    workouts:
                      - name: Broken
                        exercises:
                          - name: 400m
                            sets: [
                """;

        assertThrows(
                WorkoutUploadException.class,
                () -> service.importProgramme(stream(yaml))
        );
    }

    @Test
    void rejectsInvalidProgramme() {
        String yaml = """
                name: Invalid Programme
                weeks:
                  - number: 1
                    workouts: []
                """;

        assertThrows(
                UploadValidationException.class,
                () -> service.importProgramme(stream(yaml))
        );
    }

    private ByteArrayInputStream stream(String yaml) {
        return new ByteArrayInputStream(
                yaml.getBytes(StandardCharsets.UTF_8)
        );
    }
}