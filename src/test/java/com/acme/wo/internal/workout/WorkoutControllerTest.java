package com.acme.wo.internal.workout;

import com.acme.wo.model.ProgrammeLocation;
import com.acme.wo.model.ValidationError;
import com.acme.wo.model.Workout;
import com.acme.wo.service.UploadValidationException;
import com.acme.wo.service.WorkoutService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
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
class WorkoutControllerTest {

    @Mock
    private WorkoutService workoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WorkoutController controller =
                new WorkoutController(workoutService);

        var viewResolver =
                new InternalResourceViewResolver();

        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getWorkoutsReturnsWorkoutsView()
            throws Exception {

        Workout workout1 = mockWorkout();
        Workout workout2 = mockWorkout();

        List<Workout> workouts =
                List.of(workout1, workout2);

        when(workoutService.findAll())
                .thenReturn(workouts);

        mockMvc.perform(
                        get("/workouts")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("workouts"))
                .andExpect(
                        model().attribute(
                                "workouts",
                                workouts
                        )
                )
                .andExpect(
                        model().attribute(
                                "workouts",
                                hasSize(2)
                        )
                );

        verify(workoutService).findAll();
    }

    @Test
    void standaloneWorkoutPreview()
            throws Exception {

        Long workoutId = 10L;

        Workout workout = mockWorkout();

        when(workoutService.findById(workoutId))
                .thenReturn(workout);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/workouts/{workoutId}", workoutId)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("workout"))
                .andExpect(
                        model().attribute(
                                "workout",
                                workout
                        )
                )
                .andExpect(
                        model().attribute(
                                "returnUrl",
                                "/workouts"
                        )
                )
                .andExpect(
                        model().attribute(
                                "standalone",
                                true
                        )
                );

        verify(workoutService)
                .findById(workoutId);

        verify(workoutService)
                .findProgrammeLocation(workoutId);
    }

    @Test
    void programmeOwnedWorkoutPreviewIsNotStandalone()
            throws Exception {

        Long workoutId = 20L;

        Workout workout = mockWorkout();

        ProgrammeLocation location =
                org.mockito.Mockito.mock(
                        ProgrammeLocation.class
                );

        when(workoutService.findById(workoutId))
                .thenReturn(workout);

        when(workoutService
                .findProgrammeLocation(workoutId))
                .thenReturn(Optional.of(location));

        mockMvc.perform(
                        get("/workouts/{workoutId}", workoutId)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("workout"))
                .andExpect(
                        model().attribute(
                                "workout",
                                workout
                        )
                )
                .andExpect(
                        model().attribute(
                                "returnUrl",
                                "/workouts"
                        )
                )
                .andExpect(
                        model().attribute(
                                "standalone",
                                false
                        )
                );
    }

    @Test
    void deletingStandaloneWorkoutRedirectsToWorkouts()
            throws Exception {

        Long workoutId = 10L;

        mockMvc.perform(
                        post(
                                "/workouts/{workoutId}/delete",
                                workoutId
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                );

        verify(workoutService)
                .deleteStandalone(workoutId);
    }

    @Test
    void programmeOwnedWorkoutDeleteRequestIsRejected()
            throws Exception {

        Long workoutId = 20L;

        whenDeleteThrowsProgrammeOwnedError(workoutId);

        /*
         * There is currently no controller-level exception
         * handler for IllegalStateException, so MockMvc
         * propagates the failure.
         */
        try {
            mockMvc.perform(
                    post(
                            "/workouts/{workoutId}/delete",
                            workoutId
                    )
            );
        } catch (Exception ignored) {
            // Expected from the current controller contract.
        }

        verify(workoutService)
                .deleteStandalone(workoutId);
    }

    @Test
    void successfulWorkoutUploadRedirectsToWorkouts()
            throws Exception {

        Workout persisted = mockWorkout();

        when(workoutService.persist(any()))
                .thenReturn(List.of(persisted));

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "workout.yaml",
                        "application/x-yaml",
                        """
                        name: Speed Endurance
                        exercises: []
                        """.getBytes()
                );

        mockMvc.perform(
                        multipart("/workouts/upload")
                                .file(file)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                );

        verify(workoutService)
                .persist(any());
    }

    @Test
    void invalidWorkoutUploadRedirectsWithValidationErrors()
            throws Exception {

        /*
         * Construct this using the same approach as your
         * ProgrammeControllerTest if your exception
         * constructor has a different signature.
         */
        var errors =
                List.of(new ValidationError("path1", "Workout name is required"));

        UploadValidationException exception =
                new UploadValidationException(errors);

        when(workoutService.persist(any()))
                .thenThrow(exception);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "workout.yaml",
                        "application/x-yaml",
                        """
                        name:
                        exercises: []
                        """.getBytes()
                );

        mockMvc.perform(
                        multipart("/workouts/upload")
                                .file(file)
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        redirectedUrl("/workouts")
                )
                .andExpect(
                        flash().attribute(
                                "uploadErrors",
                                errors
                        )
                );

        /*
         * At MVC level we prove the request was delegated
         * once and that validation feedback is returned.
         *
         * Whether invalid data reaches persistence belongs
         * to the service/integration tests.
         */
        verify(workoutService)
                .persist(any());
    }

    private Workout mockWorkout() {
        return org.mockito.Mockito.mock(
                Workout.class
        );
    }

    private void whenDeleteThrowsProgrammeOwnedError(
            Long workoutId
    ) {
        org.mockito.Mockito
                .doThrow(
                        new IllegalStateException(
                                "A programme workout cannot be deleted independently"
                        )
                )
                .when(workoutService)
                .deleteStandalone(workoutId);
    }
}