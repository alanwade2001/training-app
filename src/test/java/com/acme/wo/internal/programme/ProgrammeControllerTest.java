package com.acme.wo.internal.programme;

import com.acme.wo.model.Programme;
import com.acme.wo.model.ValidationError;
import com.acme.wo.model.Week;
import com.acme.wo.model.Workout;
import com.acme.wo.service.ProgrammeService;
import com.acme.wo.service.SessionService;
import com.acme.wo.service.UploadValidationException;
import com.acme.wo.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ProgrammeControllerTest {

    @Mock
    private ProgrammeService programmeService;

    @Mock
    private SessionService sessionService;

    @Mock
    private WorkoutService workoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProgrammeController controller =
                new ProgrammeController(
                        programmeService,
                        sessionService,
                        workoutService
                );

        var viewResolver =
                new org.springframework.web.servlet.view
                        .InternalResourceViewResolver();

        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getProgrammesReturnsProgrammesViewWithProgrammes()
            throws Exception {

        Programme programme1 =
                programme(
                        1L,
                        "400m Programme",
                        List.of()
                );

        Programme programme2 =
                programme(
                        2L,
                        "Strength Programme",
                        List.of()
                );

        List<Programme> programmes =
                List.of(programme1, programme2);

        when(programmeService.findAll())
                .thenReturn(programmes);

        mockMvc.perform(
                        get("/programmes")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("programmes"))
                .andExpect(
                        model().attribute(
                                "programmes",
                                programmes
                        )
                );

        verify(programmeService).findAll();
    }

    @Test
    void getProgrammeReturnsProgrammeViewWithWeekProgress()
            throws Exception {

        Workout workout10 = workout(10L, "Workout A");
        Workout workout11 = workout(11L, "Workout B");

        Week week =
                week(
                        1,
                        List.of(
                                workout10,
                                workout11
                        )
                );

        Programme programme =
                programme(
                        1L,
                        "400m Programme",
                        List.of(week)
                );

        when(programmeService.findById(1L))
                .thenReturn(programme);

        when(sessionService
                .countCompletedByWorkoutId(10L))
                .thenReturn(1L);

        when(sessionService
                .countCompletedByWorkoutId(11L))
                .thenReturn(0L);

        mockMvc.perform(
                        get("/programmes/1")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("programme"))
                .andExpect(
                        model().attribute(
                                "programme",
                                programme
                        )
                )
                .andExpect(
                        model().attribute(
                                "weeks",
                                hasSize(1)
                        )
                )
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<WeekProgress> weeks =
                            (List<WeekProgress>) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("weeks");

                    WeekProgress progress =
                            weeks.getFirst();

                    assertEquals(
                            1,
                            progress.completedWorkouts()
                    );

                    assertEquals(
                            2,
                            progress.totalWorkouts()
                    );

                    assertFalse(progress.done());
                });
    }

    @Test
    void programmeProgressCountsWorkoutOnceDespiteRepeatedCompletions()
            throws Exception {

        Workout workout10 = workout(10L, "Workout A");
        Workout workout11 = workout(11L, "Workout B");

        Week week =
                week(
                        1,
                        List.of(
                                workout10,
                                workout11
                        )
                );

        Programme programme =
                programme(
                        1L,
                        "400m Programme",
                        List.of(week)
                );

        when(programmeService.findById(1L))
                .thenReturn(programme);

        /*
         * Workout A has been completed three times.
         * It should still contribute exactly one
         * completed planned workout.
         */
        when(sessionService
                .countCompletedByWorkoutId(10L))
                .thenReturn(3L);

        when(sessionService
                .countCompletedByWorkoutId(11L))
                .thenReturn(0L);

        mockMvc.perform(
                        get("/programmes/1")
                )
                .andExpect(status().isOk())
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<WeekProgress> weeks =
                            (List<WeekProgress>) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("weeks");

                    WeekProgress progress =
                            weeks.getFirst();

                    assertEquals(
                            1,
                            progress.completedWorkouts()
                    );

                    assertEquals(
                            2,
                            progress.totalWorkouts()
                    );

                    assertFalse(progress.done());
                });
    }

    @Test
    void completedProgrammeWeekIsMarkedDone()
            throws Exception {

        Workout workout10 = workout(10L, "Workout A");
        Workout workout11 = workout(11L, "Workout B");

        Week week =
                week(
                        1,
                        List.of(
                                workout10,
                                workout11
                        )
                );

        Programme programme =
                programme(
                        1L,
                        "400m Programme",
                        List.of(week)
                );

        when(programmeService.findById(1L))
                .thenReturn(programme);

        when(sessionService
                .countCompletedByWorkoutId(10L))
                .thenReturn(2L);

        when(sessionService
                .countCompletedByWorkoutId(11L))
                .thenReturn(1L);

        mockMvc.perform(
                        get("/programmes/1")
                )
                .andExpect(status().isOk())
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<WeekProgress> weeks =
                            (List<WeekProgress>) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("weeks");

                    WeekProgress progress =
                            weeks.getFirst();

                    assertEquals(
                            2,
                            progress.completedWorkouts()
                    );

                    assertEquals(
                            2,
                            progress.totalWorkouts()
                    );

                    assertTrue(progress.done());
                });
    }

    @Test
    void getWeekReturnsWeekViewWithWorkoutProgress()
            throws Exception {

        Workout workout10 = workout(10L, "Workout A");
        Workout workout11 = workout(11L, "Workout B");

        Week week =
                week(
                        2,
                        List.of(
                                workout10,
                                workout11
                        )
                );

        Programme programme =
                programme(
                        1L,
                        "400m Programme",
                        List.of(week)
                );

        when(programmeService.findById(1L))
                .thenReturn(programme);

        when(sessionService.countByWorkoutId(10L))
                .thenReturn(3L);

        when(sessionService
                .countCompletedByWorkoutId(10L))
                .thenReturn(1L);

        when(sessionService.countByWorkoutId(11L))
                .thenReturn(2L);

        when(sessionService
                .countCompletedByWorkoutId(11L))
                .thenReturn(0L);

        mockMvc.perform(
                        get("/programmes/1/weeks/2")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("week"))
                .andExpect(
                        model().attribute(
                                "programme",
                                programme
                        )
                )
                .andExpect(
                        model().attribute(
                                "week",
                                week
                        )
                )
                .andExpect(
                        model().attribute(
                                "workouts",
                                hasSize(2)
                        )
                )
                .andExpect(
                        model().attribute(
                                "completedWorkouts",
                                1L
                        )
                );
    }

    @Test
    void weekCompletedWorkoutsUsesCompletedSessionsRatherThanAttempts()
            throws Exception {

        Workout workout10 =
                workout(10L, "Incomplete Workout");

        Workout workout11 =
                workout(11L, "Completed Workout");

        Week week =
                week(
                        1,
                        List.of(
                                workout10,
                                workout11
                        )
                );

        Programme programme =
                programme(
                        1L,
                        "400m Programme",
                        List.of(week)
                );

        when(programmeService.findById(1L))
                .thenReturn(programme);

        /*
         * Workout 10 has three attempts but none completed.
         */
        when(sessionService.countByWorkoutId(10L))
                .thenReturn(3L);

        when(sessionService
                .countCompletedByWorkoutId(10L))
                .thenReturn(0L);

        /*
         * Workout 11 has two attempts with one completion.
         */
        when(sessionService.countByWorkoutId(11L))
                .thenReturn(2L);

        when(sessionService
                .countCompletedByWorkoutId(11L))
                .thenReturn(1L);

        mockMvc.perform(
                        get("/programmes/1/weeks/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute(
                                "completedWorkouts",
                                1L
                        )
                );
    }

    @Test
    void successfulUploadRedirectsToCreatedProgramme()
            throws Exception {

        Programme persisted =
                programme(
                        42L,
                        "Uploaded Programme",
                        List.of()
                );

        when(programmeService.persist(any()))
                .thenReturn(persisted);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "programme.yaml",
                        "application/x-yaml",
                        """
                        name: Uploaded Programme
                        weeks: []
                        """.getBytes()
                );

        mockMvc.perform(
                        multipart("/programmes/upload")
                                .file(file)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl(
                                "/programmes/42"
                        )
                );

        verify(programmeService)
                .persist(any());
    }

    @Test
    void failedUploadRedirectsWithValidationErrors()
            throws Exception {

        /*
         * Replace this construction if your
         * UploadValidationException constructor differs.
         */
        var errors =
                List.of(new ValidationError("path", "Programme name is required"));

        UploadValidationException exception =
                new UploadValidationException(errors);

        when(programmeService.persist(any()))
                .thenThrow(exception);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "programme.yaml",
                        "application/x-yaml",
                        """
                        name:
                        weeks: []
                        """.getBytes()
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
                        flash().attribute(
                                "uploadErrors",
                                errors
                        )
                );
    }

    @Test
    void deletingProgrammeRedirectsToProgrammes()
            throws Exception {

        mockMvc.perform(
                        post("/programmes/10/delete")
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/programmes")
                );

        verify(programmeService)
                .deleteProgramme(10L);
    }

    private Programme programme(
            Long id,
            String name,
            List<Week> weeks
    ) {
        return new Programme(
                id,
                name,
                weeks
        );
    }

    private Week week(
            int number,
            List<Workout> workouts
    ) {
        return new Week(
                number,
                workouts
        );
    }

    private Workout workout(
            Long id,
            String name
    ) {
        return org.mockito.Mockito.mock(
                Workout.class,
                invocation -> {
                    if (invocation.getMethod()
                            .getName()
                            .equals("id")) {
                        return id;
                    }

                    if (invocation.getMethod()
                            .getName()
                            .equals("name")) {
                        return name;
                    }

                    return org.mockito.Mockito
                            .RETURNS_DEFAULTS
                            .answer(invocation);
                }
        );
    }
}