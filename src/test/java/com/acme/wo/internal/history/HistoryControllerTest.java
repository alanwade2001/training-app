package com.acme.wo.internal.history;

import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExercise;
import com.acme.wo.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class HistoryControllerTest {

    @Mock
    private SessionService sessionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HistoryController controller =
                new HistoryController(sessionService);

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
    void getHistoryReturnsHistoryViewWithSessions()
            throws Exception {

        Session session1 = mock(Session.class);
        Session session2 = mock(Session.class);

        List<Session> sessions =
                List.of(session1, session2);

        when(sessionService.history())
                .thenReturn(sessions);

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(
                        model().attribute(
                                "sessions",
                                sessions
                        )
                )
                .andExpect(
                        model().attribute(
                                "sessions",
                                hasSize(2)
                        )
                );

        verify(sessionService).history();
    }

    @Test
    void getHistoryDetailReturnsSessionDetailWithSnapshot()
            throws Exception {

        Long sessionId = 100L;

        SessionExercise snapshotExercise =
                mock(SessionExercise.class);

        Session historicalSession =
                mock(Session.class);

        when(historicalSession.exercises())
                .thenReturn(
                        List.of(snapshotExercise)
                );

        when(sessionService.findById(sessionId))
                .thenReturn(historicalSession);

        mockMvc.perform(
                        get(
                                "/history/{sessionId}",
                                sessionId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        view().name("session-detail")
                )
                .andExpect(
                        model().attribute(
                                "trainingsession",
                                historicalSession
                        )
                )
                .andExpect(result -> {
                    Session modelSession =
                            (Session) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("trainingsession");

                    org.junit.jupiter.api.Assertions
                            .assertEquals(
                                    1,
                                    modelSession
                                            .exercises()
                                            .size()
                            );

                    org.junit.jupiter.api.Assertions
                            .assertSame(
                                    snapshotExercise,
                                    modelSession
                                            .exercises()
                                            .getFirst()
                            );
                });

        verify(sessionService)
                .findById(sessionId);
    }
}