package com.acme.wo.regression;

import com.acme.wo.model.ExecutionState;
import com.acme.wo.model.Session;
import com.acme.wo.service.SessionService;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class SessionJourneySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JourneyState state;

    @And("I start the Session")
    public void startSession() throws Exception {
        assertNotNull(
                state.getWorkoutId(),
                "Workout ID has not been initialised"
        );

        MvcResult result =
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
                result.getResponse()
                        .getRedirectedUrl();

        assertNotNull(
                redirect,
                "Session creation did not redirect"
        );

        state.setSessionId(
                idFromRedirect(
                        redirect,
                        "/sessions/"
                )
        );

        state.setLastResult(result);
    }

    @And("I complete the Session")
    public void completeSession()
            throws Exception {

        assertNotNull(
                state.getSessionId(),
                "Session ID has not been initialised"
        );

        int transitionCount = 0;

        while (true) {
            Session session =
                    sessionService.findById(
                            state.getSessionId()
                    );

            assertNotNull(
                    session,
                    "Session could not be found"
            );

            ExecutionState executionState =
                    session.execution().state();

            if (executionState
                    == ExecutionState.SESSION_COMPLETE) {
                return;
            }

            switch (executionState) {
                case READY ->
                        performStateAction(
                                "/sessions/{sessionId}/sets/start"
                        );

                case ACTIVE ->
                        performStateAction(
                                "/sessions/{sessionId}/sets/complete"
                        );

                case RESTING ->
                        performStateAction(
                                "/sessions/{sessionId}/rest/complete"
                        );

                case EXERCISE_COMPLETE ->
                        performStateAction(
                                "/sessions/{sessionId}/exercises/next"
                        );

                default ->
                        throw new AssertionError(
                                "Unexpected execution state: "
                                        + executionState
                        );
            }

            transitionCount++;

            /*
             * Protect the test against an accidental infinite
             * loop if the state stops advancing.
             */
            if (transitionCount > 100) {
                throw new AssertionError(
                        "Session did not complete after "
                                + transitionCount
                                + " transitions"
                );
            }
        }
    }

    @And("I select DONE")
    public void selectDone() throws Exception {
        assertNotNull(
                state.getSessionId(),
                "Session ID has not been initialised"
        );

        Session session =
                sessionService.findById(
                        state.getSessionId()
                );

        assertNotNull(session);

        assertEquals(
                ExecutionState.SESSION_COMPLETE,
                session.execution().state(),
                "DONE should only be selected after completion"
        );

        MvcResult result =
                mockMvc.perform(
                                post(
                                        "/sessions/{sessionId}/complete",
                                        state.getSessionId()
                                )
                        )
                        .andExpect(
                                status().is3xxRedirection()
                        )
                        .andReturn();

        state.setLastResult(result);
    }

    @And("the Session appears in History")
    public void sessionAppearsInHistory()
            throws Exception {

        assertNotNull(
                state.getSessionId(),
                "Session ID has not been initialised"
        );

        boolean present =
                sessionService.history()
                        .stream()
                        .anyMatch(session ->
                                state.getSessionId()
                                        .equals(session.id())
                        );

        assertTrue(
                present,
                "Expected Session to appear in History"
        );

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"));
    }

    @And("the Session Detail is readable")
    public void sessionDetailIsReadable()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/history/{sessionId}",
                                state.getSessionId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("session-detail")
                );
    }

    @And("the historical exercise snapshot is present")
    public void historicalExerciseSnapshotIsPresent() {

        Session historical =
                sessionService.findById(
                        state.getSessionId()
                );

        assertNotNull(historical);

        assertEquals(
                1,
                historical.exercises().size()
        );

        var snapshot =
                historical.exercises().get(0);

        assertEquals(
                state.getExpectedExerciseName(),
                snapshot.name()
        );

        assertEquals(1, snapshot.sequence());
        assertEquals(1, snapshot.sets());
        assertEquals(1, snapshot.reps());
        assertEquals(180, snapshot.recoveryInSecs());
    }

    private void performStateAction(
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
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/"
                                        + state.getSessionId()
                        )
                );
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
}