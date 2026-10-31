package com.acme.wo.regression;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Session;
import com.acme.wo.model.Workout;
import com.acme.wo.service.ProgrammeService;
import com.acme.wo.service.SessionService;
import com.acme.wo.service.WorkoutService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class DeletionJourneySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JourneyState state;

    @Given("I have completed a Session for the programme Workout")
    public void completedProgrammeWorkout()
            throws Exception {

        createAndCompleteSession();
    }

    @Given("I have completed a Session for the standalone Workout")
    public void completedStandaloneWorkout()
            throws Exception {

        createAndCompleteSession();
    }

    @When("I delete the Programme")
    public void deleteProgramme()
            throws Exception {

        assertNotNull(state.getProgrammeId());

        mockMvc.perform(
                        post(
                                "/programmes/{programmeId}/delete",
                                state.getProgrammeId()
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/programmes")
                );
    }

    @Then("the Programme is no longer available")
    public void programmeNoLongerAvailable() {

        Programme programme =
                programmeService.findById(
                        state.getProgrammeId()
                );

        assertNull(programme);
    }

    @Then("the programme Workout is no longer available")
    public void programmeWorkoutNoLongerAvailable() {

        boolean present =
                workoutService.findAll()
                        .stream()
                        .anyMatch(workout ->
                                state.getWorkoutId()
                                        .equals(workout.id())
                        );

        assertFalse(
                present,
                "Deleted programme Workout is still available"
        );
    }

    @When("I delete the standalone Workout")
    public void deleteStandaloneWorkout()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/workouts/{workoutId}/delete",
                                state.getWorkoutId()
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                );
    }

    @Then("the Workout is no longer available in Workouts")
    public void workoutNoLongerAvailable() {

        boolean present =
                workoutService.findAll()
                        .stream()
                        .anyMatch(workout ->
                                state.getWorkoutId()
                                        .equals(workout.id())
                        );

        assertFalse(
                present,
                "Deleted standalone Workout is still available"
        );
    }

    @When("I open History")
    public void openHistory()
            throws Exception {

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"));
    }

    @Then("the historical Session remains available")
    public void historicalSessionRemainsAvailable() {

        Session session =
                sessionService.findById(
                        state.getSessionId()
                );

        assertNotNull(
                session,
                "Historical Session was deleted"
        );
    }

    @And("the historical Session retains its workout name")
    public void historicalSessionRetainsWorkoutName() {

        Session session =
                sessionService.findById(
                        state.getSessionId()
                );

        assertNotNull(session);

        assertEquals(
                state.getExpectedWorkoutName(),
                session.workoutName()
        );
    }

    private void createAndCompleteSession()
            throws Exception {

        assertNotNull(state.getWorkoutId());

        var createResult =
                mockMvc.perform(
                                post("/sessions")
                                        .param(
                                                "workoutId",
                                                state.getWorkoutId()
                                                        .toString()
                                        )
                        )
                        .andExpect(
                                status().is3xxRedirection()
                        )
                        .andReturn();

        String redirect =
                createResult.getResponse()
                        .getRedirectedUrl();

        assertNotNull(redirect);

        state.setSessionId(
                Long.valueOf(
                        redirect.substring(
                                "/sessions/".length()
                        )
                )
        );

        /*
         * Drive through the same service state machine used
         * by the normal Session journey.
         */
        while (true) {
            Session session =
                    sessionService.findById(
                            state.getSessionId()
                    );

            assertNotNull(session);

            switch (session.execution().state()) {

                case READY ->
                        performSessionAction(
                                "/sessions/{sessionId}/sets/start"
                        );

                case ACTIVE ->
                        performSessionAction(
                                "/sessions/{sessionId}/sets/complete"
                        );

                case RESTING ->
                        performSessionAction(
                                "/sessions/{sessionId}/rest/complete"
                        );

                case EXERCISE_COMPLETE ->
                        performSessionAction(
                                "/sessions/{sessionId}/exercises/next"
                        );

                case SESSION_COMPLETE -> {
                    return;
                }

                default ->
                        throw new AssertionError(
                                "Unexpected Session state: "
                                        + session.execution().state()
                        );
            }
        }
    }

    private void performSessionAction(
            String endpoint
    ) throws Exception {

        mockMvc.perform(
                        post(
                                endpoint,
                                state.getSessionId()
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                );
    }
}