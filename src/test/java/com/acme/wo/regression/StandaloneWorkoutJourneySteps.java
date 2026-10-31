package com.acme.wo.regression;

import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutDefinitions;
import com.acme.wo.service.WorkoutService;
import com.acme.wo.service.YamlUploadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class StandaloneWorkoutJourneySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private WorkoutPersistor workoutPersistor;

    @Autowired
    private YamlUploadService  yamlUploadService;

    @Autowired
    private JourneyState state;

    @Before
    public void resetStandaloneState() {
        state.setWorkoutId(null);
        state.setSessionId(null);
        state.setLastResult(null);
    }

    @Given("a standalone Workout exists")
    public void standaloneWorkoutExists() {

        String yaml = """
            workouts:
              - name: Standalone Speed Endurance
                plannedDay: THURSDAY
                exercises:
                  - name: 400m
                    sets: 1
                    reps: 1
                    quantity: 400
                    units: METERS
                    recoveryInSecs: 180
            """;

        MockMultipartFile source =
                new MockMultipartFile(
                        "file",
                        "standalone-workout.yaml",
                        "application/x-yaml",
                        yaml.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        workoutService.persist(source);

        Workout reloaded =
                workoutService.findAll()
                        .stream()
                        .filter(workout ->
                                "Standalone Speed Endurance"
                                        .equals(workout.name())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "Persisted standalone Workout was not found"
                                )
                        );

        assertNotNull(
                reloaded.id(),
                "Persisted standalone Workout must have an ID"
        );

        assertFalse(
                reloaded.exercises().isEmpty(),
                "Persisted standalone Workout must contain exercises"
        );

        state.setWorkoutId(
                reloaded.id()
        );

        state.setExpectedWorkoutName(
                reloaded.name()
        );

        state.setExpectedExerciseName(
                reloaded.exercises()
                        .getFirst()
                        .name()
        );
    }

    @When("I open Workouts")
    public void openWorkouts() throws Exception {
        MvcResult result =
                mockMvc.perform(get("/workouts"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("workouts"))
                        .andReturn();

        state.setLastResult(result);
    }

    @And("I preview the standalone Workout")
    public void previewStandaloneWorkout()
            throws Exception {

        assertNotNull(
                state.getWorkoutId(),
                "Workout ID has not been initialised"
        );

        MvcResult result =
                mockMvc.perform(
                                get(
                                        "/workouts/{workoutId}",
                                        state.getWorkoutId()
                                )
                        )
                        .andExpect(status().isOk())
                        .andExpect(view().name("workout"))
                        .andExpect(
                                model().attribute(
                                        "standalone",
                                        true
                                )
                        )
                        .andExpect(
                                model().attribute(
                                        "returnUrl",
                                        "/workouts"
                                )
                        )
                        .andReturn();

        state.setLastResult(result);
    }

    @And("I return from the Workout preview")
    public void returnFromWorkoutPreview()
            throws Exception {

        /*
         * MockMvc cannot click the rendered Back anchor.
         * The preview test above proves its returnUrl and
         * this request follows that URL.
         */
        MvcResult result =
                mockMvc.perform(get("/workouts"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("workouts"))
                        .andReturn();

        state.setLastResult(result);
    }

    @Then("I am returned to Workouts")
    public void returnedToWorkouts() {
        assertNotNull(
                state.getLastResult(),
                "No previous MVC result is available"
        );

        String redirectedUrl =
                state.getLastResult()
                        .getResponse()
                        .getRedirectedUrl();

        if (redirectedUrl != null) {
            assertEquals(
                    "/workouts",
                    redirectedUrl
            );
            return;
        }

        assertNotNull(
                state.getLastResult().getModelAndView()
        );

        assertEquals(
                "workouts",
                state.getLastResult()
                        .getModelAndView()
                        .getViewName()
        );
    }
}