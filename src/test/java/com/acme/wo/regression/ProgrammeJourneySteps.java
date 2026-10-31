package com.acme.wo.regression;

import com.acme.wo.internal.data.ProgrammeRepository;
import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.model.*;
import com.acme.wo.service.ProgrammeService;
import com.acme.wo.service.SessionService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class ProgrammeJourneySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgrammePersistor programmePersistor;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JourneyState state;

    @Autowired
    private SessionJourneySteps sessionJourneySteps;

    @Before
    public void resetScenarioState() {
        state.setProgrammeId(null);
        state.setWeekNumber(0);
        state.setWorkoutId(null);
        state.setSessionId(null);
        state.setInitialCompletedWorkouts(0);
        state.setLastResult(null);
    }

    @Given("a programme contains an incomplete planned workout")
    public void programmeContainsIncompleteWorkout() {
        createProgrammeWithWorkout();
    }

    @Given("a Programme exists with a planned Workout")
    public void programmeExistsWithPlannedWorkout() {
        createProgrammeWithWorkout();
    }

    @Given("the Week has {long} completed workouts")
    public void weekHasCompletedWorkouts(
            long expectedCompletedWorkouts
    ) {
        long actual =
                completedWorkoutCount();

        assertEquals(
                expectedCompletedWorkouts,
                actual
        );

        state.setInitialCompletedWorkouts(actual);
    }

    @Given("the Workout has already been completed once")
    public void workoutAlreadyCompletedOnce()
            throws Exception {

        sessionJourneySteps.startSession();
        completeCurrentSession();
        sessionJourneySteps.selectDone();

        assertEquals(
                1,
                sessionService.countCompletedByWorkoutId(
                        state.getWorkoutId()
                )
        );

        state.setInitialCompletedWorkouts(1);
    }

    @When("I open Home")
    public void openHome() throws Exception {
        state.setLastResult(
                mockMvc.perform(get("/"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("home"))
                        .andReturn()
        );
    }

    @And("I open Programmes")
    public void openProgrammes() throws Exception {
        state.setLastResult(
                mockMvc.perform(get("/programmes"))
                        .andExpect(status().isOk())
                        .andExpect(
                                view().name("programmes")
                        )
                        .andReturn()
        );
    }

    @And("I select the Programme")
    public void selectProgramme()
            throws Exception {

        state.setLastResult(
                mockMvc.perform(
                                get(
                                        "/programmes/{programmeId}",
                                        state.getProgrammeId()
                                )
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                view().name("programme")
                        )
                        .andReturn()
        );
    }

    @And("I select the Week")
    public void selectWeek() throws Exception {
        state.setLastResult(
                mockMvc.perform(
                                get(
                                        programmeWeekUrl()
                                )
                        )
                        .andExpect(status().isOk())
                        .andExpect(view().name("week"))
                        .andReturn()
        );
    }

    @And("I preview the Workout")
    public void previewWorkout()
            throws Exception {

        state.setLastResult(
                mockMvc.perform(
                                get(
                                        programmeWorkoutUrl()
                                )
                        )
                        .andExpect(status().isOk())
                        .andExpect(view().name("workout"))
                        .andReturn()
        );
    }

    @And("I preview the Workout again")
    public void previewWorkoutAgain()
            throws Exception {

        previewWorkout();
    }

    @And("I return to the Week")
    public void returnToWeek()
            throws Exception {

        state.setLastResult(
                mockMvc.perform(
                                get(programmeWeekUrl())
                        )
                        .andExpect(status().isOk())
                        .andExpect(view().name("week"))
                        .andReturn()
        );
    }

    @Then("I am viewing the originating programme Week")
    public void viewingOriginatingWeek() {
        assertNotNull(state.getLastResult());

        assertEquals(
                "week",
                state.getLastResult()
                        .getModelAndView()
                        .getViewName()
        );
    }







    @Then("I am returned to the originating programme Week")
    public void returnedToOriginatingWeek() {
        assertNotNull(state.getLastResult());

        assertEquals(
                programmeWeekUrl(),
                state.getLastResult()
                        .getResponse()
                        .getRedirectedUrl()
        );
    }

    @And("the Workout is complete")
    public void workoutIsComplete() {
        assertTrue(
                sessionService
                        .countCompletedByWorkoutId(
                                state.getWorkoutId()
                        )
                        > 0
        );
    }

    @And("the Week has {long} completed workout")
    public void weekHasCompletedWorkout(
            long expected
    ) {
        assertEquals(
                expected,
                completedWorkoutCount()
        );

        assertEquals(
                state.getInitialCompletedWorkouts() + 1,
                completedWorkoutCount()
        );
    }

    @Then("the Week still has {long} completed workout")
    public void weekStillHasCompletedWorkout(
            long expected
    ) {
        assertEquals(
                expected,
                completedWorkoutCount()
        );

        assertEquals(
                state.getInitialCompletedWorkouts(),
                completedWorkoutCount()
        );
    }

    @And("the Workout has {long} completed Sessions")
    public void workoutHasCompletedSessions(
            long expected
    ) {
        assertEquals(
                expected,
                sessionService
                        .countCompletedByWorkoutId(
                                state.getWorkoutId()
                        )
        );
    }



    @And("both Sessions appear in History")
    public void bothSessionsAppearInHistory() {
        long sessionCount =
                sessionService.history()
                        .stream()
                        .filter(session ->
                                state.getWorkoutId()
                                        .equals(
                                                session.workoutId()
                                        )
                        )
                        .count();

        assertEquals(2, sessionCount);
    }

    @And("the Programme Week is no longer available")
    public void programmeWeekNoLongerAvailable()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/programmes/{programmeId}/weeks/{weekNumber}",
                                state.getProgrammeId(),
                                state.getWeekNumber()
                        )
                )
                .andExpect(
                        status().is4xxClientError()
                );
    }






    private void completeCurrentSession()
            throws Exception {

        while (true) {
            Session session =
                    sessionService.findById(
                            state.getSessionId()
                    );

            assertNotNull(session);

            ExecutionState executionState =
                    session.execution().state();

            switch (executionState) {
                case READY ->
                        performAction(
                                "/sessions/{sessionId}/sets/start"
                        );

                case ACTIVE ->
                        performAction(
                                "/sessions/{sessionId}/sets/complete"
                        );

                case RESTING ->
                        performAction(
                                "/sessions/{sessionId}/rest/complete"
                        );

                case EXERCISE_COMPLETE ->
                        performAction(
                                "/sessions/{sessionId}/exercises/next"
                        );

                case SESSION_COMPLETE -> {
                    return;
                }

                default ->
                        throw new AssertionError(
                                "Unexpected session state: "
                                        + executionState
                        );
            }
        }
    }

    private void performAction(
            String endpoint
    ) throws Exception {

        mockMvc.perform(
                        post(
                                endpoint,
                                state.getSessionId()
                        )
                )
                .andExpect(
                        status()
                                .is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/"
                                        + state.getSessionId()
                        )
                );
    }

    private long completedWorkoutCount() {
        Programme programme =
                programmeService.findById(
                        state.getProgrammeId()
                );

        assertNotNull(programme);

        return programme.weeks()
                .stream()
                .filter(week ->
                        week.number()
                                == state.getWeekNumber()
                )
                .flatMap(week ->
                        week.workouts().stream()
                )
                .filter(workout ->
                        sessionService
                                .countCompletedByWorkoutId(
                                        workout.id()
                                )
                                > 0
                )
                .count();
    }

    private String programmeWeekUrl() {
        return "/programmes/"
                + state.getProgrammeId()
                + "/weeks/"
                + state.getWeekNumber();
    }

    private String programmeWorkoutUrl() {
        assertNotNull(
                state.getProgrammeId(),
                "Programme ID has not been initialised"
        );

        assertNotNull(
                state.getWorkoutId(),
                "Workout ID has not been initialised"
        );

        return "/programmes/"
                + state.getProgrammeId()
                + "/weeks/"
                + state.getWeekNumber()
                + "/workouts/"
                + state.getWorkoutId();
    }

    private Long idFromRedirect(
            String redirect,
            String prefix
    ) {
        assertTrue(
                redirect.startsWith(prefix),
                "Unexpected redirect: " + redirect
        );

        return Long.valueOf(
                redirect.substring(prefix.length())
        );
    }

    private void createProgrammeWithWorkout() {
        WorkoutExercise exercise =
                new WorkoutExercise(
                        1,
                        "400m",
                        1,
                        1,
                        400,
                        ExerciseUnit.METERS,
                        180
                );

        Workout workout =
                new Workout(
                        null,
                        "Programme Speed Endurance",
                        DayOfWeek.THURSDAY,
                        List.of(exercise)
                );

        Week week =
                new Week(
                        1,
                        List.of(workout)
                );

        Programme programme =
                new Programme(
                        null,
                        "Cucumber Programme",
                        List.of(week)
                );

        Programme persisted =
                programmePersistor.persist(programme);

        Programme reloaded =
                programmeService.findById(
                        persisted.id()
                );

        assertNotNull(reloaded);
        assertFalse(reloaded.weeks().isEmpty());

        Week persistedWeek =
                reloaded.weeks().getFirst();

        assertFalse(
                persistedWeek.workouts().isEmpty()
        );

        Workout persistedWorkout =
                persistedWeek.workouts().getFirst();

        assertNotNull(
                persistedWorkout.id(),
                "Persisted Workout must have an ID"
        );

        state.setProgrammeId(reloaded.id());
        state.setWeekNumber(persistedWeek.number());
        state.setWorkoutId(persistedWorkout.id());

        state.setExpectedWorkoutName(
                persistedWorkout.name()
        );

        state.setExpectedExerciseName(
                persistedWorkout.exercises()
                        .getFirst()
                        .name()
        );
    }
}