package com.acme.wo.internal.session;

import com.acme.wo.model.ProgrammeLocation;
import com.acme.wo.model.Session;
import com.acme.wo.service.SessionService;
import com.acme.wo.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private WorkoutService workoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SessionController controller =
                new SessionController(
                        sessionService,
                        workoutService
                );

        /*
         * Unlike the previous no-op resolver, this preserves
         * Spring's redirect: handling correctly.
         */
        InternalResourceViewResolver viewResolver =
                new InternalResourceViewResolver();

        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void createsSessionForWorkout()
            throws Exception {

        Long workoutId = 10L;
        Long sessionId = 100L;

        Session session = mock(Session.class);

        when(session.id())
                .thenReturn(sessionId);

        when(sessionService.createSession(workoutId))
                .thenReturn(session);

        mockMvc.perform(
                        post("/sessions")
                                .param(
                                        "workoutId",
                                        workoutId.toString()
                                )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/" + sessionId
                        )
                );

        verify(sessionService)
                .createSession(workoutId);
    }

    @Test
    void exitsProgrammeSessionBackToProgrammeWeek()
            throws Exception {

        Long sessionId = 100L;
        Long workoutId = 10L;

        Session session = session(workoutId);

        ProgrammeLocation location =
                mock(ProgrammeLocation.class);

        when(location.programmeId())
                .thenReturn(20L);

        when(location.weekNumber())
                .thenReturn(3);

        when(sessionService.findById(sessionId))
                .thenReturn(session);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.of(location));

        mockMvc.perform(
                        get(
                                "/sessions/{sessionId}/exit",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/programmes/20/weeks/3"
                        )
                );

        verify(sessionService)
                .findById(sessionId);

        verify(workoutService)
                .findProgrammeLocation(workoutId);
    }

    @Test
    void exitsStandaloneSessionBackToWorkouts()
            throws Exception {

        Long sessionId = 100L;
        Long workoutId = 10L;

        Session session = session(workoutId);

        when(sessionService.findById(sessionId))
                .thenReturn(session);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/sessions/{sessionId}/exit",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                );

        verify(sessionService)
                .findById(sessionId);

        verify(workoutService)
                .findProgrammeLocation(workoutId);
    }

    @Test
    void exitDoesNotMutateSession()
            throws Exception {

        Long sessionId = 100L;
        Long workoutId = 10L;

        Session session = session(workoutId);

        when(sessionService.findById(sessionId))
                .thenReturn(session);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/sessions/{sessionId}/exit",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                );

        verify(sessionService)
                .findById(sessionId);

        verify(sessionService, never())
                .startSet(sessionId);

        verify(sessionService, never())
                .completeSet(sessionId);

        verify(sessionService, never())
                .completeRest(sessionId);

        verify(sessionService, never())
                .nextExercise(sessionId);
    }

    @Test
    void completedProgrammeSessionReturnsToProgrammeWeek()
            throws Exception {

        Long sessionId = 100L;
        Long workoutId = 10L;

        Session session = session(workoutId);

        ProgrammeLocation location =
                mock(ProgrammeLocation.class);

        when(location.programmeId())
                .thenReturn(20L);

        when(location.weekNumber())
                .thenReturn(3);

        when(sessionService.findById(sessionId))
                .thenReturn(session);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.of(location));

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/complete",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/programmes/20/weeks/3"
                        )
                );

        verify(sessionService)
                .findById(sessionId);
    }

    @Test
    void completedStandaloneSessionReturnsToWorkouts()
            throws Exception {

        Long sessionId = 100L;
        Long workoutId = 10L;

        Session session = session(workoutId);

        when(sessionService.findById(sessionId))
                .thenReturn(session);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/complete",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                );
    }

    @Test
    void startSetInvokesServiceAndReturnsToSession()
            throws Exception {

        Long sessionId = 100L;

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/sets/start",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/" + sessionId
                        )
                );

        verify(sessionService)
                .startSet(sessionId);
    }

    @Test
    void completeSetInvokesServiceAndReturnsToSession()
            throws Exception {

        Long sessionId = 100L;

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/sets/complete",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/" + sessionId
                        )
                );

        verify(sessionService)
                .completeSet(sessionId);
    }

    @Test
    void completeRestInvokesServiceAndReturnsToSession()
            throws Exception {

        Long sessionId = 100L;

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/rest/complete",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/" + sessionId
                        )
                );

        verify(sessionService)
                .completeRest(sessionId);
    }

    @Test
    void nextExerciseInvokesServiceAndReturnsToSession()
            throws Exception {

        Long sessionId = 100L;

        mockMvc.perform(
                        post(
                                "/sessions/{sessionId}/exercises/next",
                                sessionId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/sessions/" + sessionId
                        )
                );

        verify(sessionService)
                .nextExercise(sessionId);
    }

    private Session session(Long workoutId) {
        Session session = mock(Session.class);

        when(session.workoutId())
                .thenReturn(workoutId);

        return session;
    }
}