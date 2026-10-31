package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.ProgrammeEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProgrammeUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void validMultipartYamlPersistsCompleteProgrammeHierarchy()
            throws Exception {

        String programmeName =
                "Integration Test Programme";

        String yaml = """
                name: Integration Test Programme
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
                            units: KG
                            recoveryInSecs: 120
                          - name: 100m
                            sets: 2
                            reps: 2
                            quantity: 100
                            units: KG
                            recoveryInSecs: 60

                  - number: 2
                    workouts:
                      - name: Speed Endurance
                        plannedDay: THURSDAY
                        exercises:
                          - name: 400m
                            sets: 3
                            reps: 1
                            quantity: 400
                            units: KG
                            recoveryInSecs: 180
                """;

        MockMultipartFile file =
                yamlFile(
                        "programme.yaml",
                        yaml
                );

        mockMvc.perform(
                        multipart("/programmes/upload")
                                .file(file)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrlPattern(
                                "/programmes/*"
                        )
                );

        /*
         * Force pending inserts to the database and make
         * subsequent queries reconstruct the hierarchy.
         */
        entityManager.flush();
        entityManager.clear();

        ProgrammeEntity persisted =
                findProgrammeByName(programmeName);

        assertNotNull(persisted.getId());

        assertEquals(
                programmeName,
                persisted.getName()
        );

        assertEquals(
                2,
                persisted.getWeeks().size()
        );

        var week1 = persisted.getWeeks()
                .stream()
                .filter(week -> week.getNumber() == 1)
                .findFirst()
                .orElseThrow();

        var week2 = persisted.getWeeks()
                .stream()
                .filter(week -> week.getNumber() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(
                persisted.getId(),
                week1.getProgramme().getId()
        );

        assertEquals(
                persisted.getId(),
                week2.getProgramme().getId()
        );

        assertEquals(
                1,
                week1.getWorkouts().size()
        );

        assertEquals(
                1,
                week2.getWorkouts().size()
        );

        var speed =
                week1.getWorkouts().getFirst();

        assertEquals(
                "Speed",
                speed.getName()
        );

        assertEquals(
                java.time.DayOfWeek.TUESDAY,
                speed.getPlannedDay()
        );

        assertEquals(
                week1.getId(),
                speed.getWeek().getId()
        );

        assertEquals(
                2,
                speed.getExercises().size()
        );

        var speedExercises =
                speed.getExercises()
                        .stream()
                        .sorted(
                                java.util.Comparator.comparingInt(
                                        exercise ->
                                                exercise.getSequence()
                                )
                        )
                        .toList();

        assertEquals(
                1,
                speedExercises.getFirst().getSequence()
        );

        assertEquals(
                "200m",
                speedExercises.getFirst().getName()
        );

        assertEquals(
                120,
                speedExercises
                        .getFirst()
                        .getRecoveryInSecs()
        );

        assertEquals(
                speed.getId(),
                speedExercises
                        .getFirst()
                        .getWorkout()
                        .getId()
        );

        assertEquals(
                2,
                speedExercises.get(1).getSequence()
        );

        assertEquals(
                "100m",
                speedExercises.get(1).getName()
        );

        var speedEndurance =
                week2.getWorkouts().getFirst();

        assertEquals(
                "Speed Endurance",
                speedEndurance.getName()
        );

        assertEquals(
                java.time.DayOfWeek.THURSDAY,
                speedEndurance.getPlannedDay()
        );

        assertEquals(
                1,
                speedEndurance.getExercises().size()
        );

        assertEquals(
                "400m",
                speedEndurance
                        .getExercises()
                        .getFirst()
                        .getName()
        );

        assertEquals(
                180,
                speedEndurance
                        .getExercises()
                        .getFirst()
                        .getRecoveryInSecs()
        );
    }

    @Test
    void invalidNestedRecoveryDoesNotPartiallyPersistProgramme()
            throws Exception {

        long programmesBefore =
                count("ProgrammeEntity");

        long weeksBefore =
                count("WeekEntity");

        long workoutsBefore =
                count("WorkoutEntity");

        long exercisesBefore =
                count("WorkoutExerciseEntity");

        String yaml = """
                name: Invalid Recovery Programme
                weeks:
                  - number: 1
                    workouts:
                      - name: Invalid Workout
                        plannedDay: TUESDAY
                        exercises:
                          - name: Invalid Exercise
                            sets: 2
                            reps: 3
                            quantity: 200
                            units: KG
                            recoveryInSecs: 14
                """;

        MockMultipartFile file =
                yamlFile(
                        "invalid-programme.yaml",
                        yaml
                );

        mockMvc.perform(
                        multipart("/programmes/upload")
                                .file(file)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/programmes")
                )
                .andExpect(
                        flash().attributeExists(
                                "uploadErrors"
                        )
                );

        entityManager.flush();
        entityManager.clear();

        assertEquals(
                programmesBefore,
                count("ProgrammeEntity")
        );

        assertEquals(
                weeksBefore,
                count("WeekEntity")
        );

        assertEquals(
                workoutsBefore,
                count("WorkoutEntity")
        );

        assertEquals(
                exercisesBefore,
                count("WorkoutExerciseEntity")
        );

        assertEquals(
                0,
                entityManager.createQuery(
                                """
                                select count(p)
                                from ProgrammeEntity p
                                where p.name = :name
                                """,
                                Long.class
                        )
                        .setParameter(
                                "name",
                                "Invalid Recovery Programme"
                        )
                        .getSingleResult()
        );
    }

    private ProgrammeEntity findProgrammeByName(
            String name
    ) {
        return entityManager.createQuery(
                        """
                        select distinct p
                        from ProgrammeEntity p
                        left join fetch p.weeks
                        where p.name = :name
                        """,
                        ProgrammeEntity.class
                )
                .setParameter("name", name)
                .getSingleResult();
    }

    private long count(String entityName) {
        return entityManager.createQuery(
                        "select count(e) from "
                                + entityName
                                + " e",
                        Long.class
                )
                .getSingleResult();
    }

    private MockMultipartFile yamlFile(
            String filename,
            String yaml
    ) {
        return new MockMultipartFile(
                "file",
                filename,
                "application/x-yaml",
                yaml.getBytes(StandardCharsets.UTF_8)
        );
    }
}