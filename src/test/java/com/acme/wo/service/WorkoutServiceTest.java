package com.acme.wo.service;

import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.ProgrammeLocation;
import com.acme.wo.model.Workout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutSource standaloneSource;

    @Mock
    private WorkoutSource programmeSource;

    @Mock
    private YamlUploadService uploadService;

    @Mock
    private WorkoutPersistor workoutPersistor;


    private WorkoutService service;

    @BeforeEach
    void setUp() {
        service = new WorkoutService(
                List.of(
                        standaloneSource,
                        programmeSource
                ),
                uploadService,
                workoutPersistor
        );
    }

    @Test
    void findsStandaloneWorkoutById() {
        Workout standalone =
                mockWorkout(10L, "Standalone Workout");

        when(standaloneSource.getWorkouts())
                .thenReturn(List.of(standalone));

        when(programmeSource.getWorkouts())
                .thenReturn(List.of());

        Workout result = service.findById(10L);

        assertSame(standalone, result);
    }

    @Test
    void findsProgrammeOwnedWorkoutById() {
        Workout programmeWorkout =
                mockWorkout(20L, "Programme Workout");

        when(standaloneSource.getWorkouts())
                .thenReturn(List.of());

        when(programmeSource.getWorkouts())
                .thenReturn(List.of(programmeWorkout));

        Workout result = service.findById(20L);

        assertSame(programmeWorkout, result);
    }

    @Test
    void returnsAllWorkoutsSortedById() {
        Workout workout30 =
                mockWorkout(30L, "Workout 30");

        Workout workout10 =
                mockWorkout(10L, "Workout 10");

        Workout workout20 =
                mockWorkout(20L, "Workout 20");

        when(standaloneSource.getWorkouts())
                .thenReturn(
                        List.of(
                                workout30,
                                workout10
                        )
                );

        when(programmeSource.getWorkouts())
                .thenReturn(
                        List.of(workout20)
                );

        List<Workout> result = service.findAll();

        assertEquals(
                List.of(10L, 20L, 30L),
                result.stream()
                        .map(Workout::id)
                        .toList()
        );
    }

    @Test
    void returnsProgrammeLocationForProgrammeOwnedWorkout() {
        Long workoutId = 20L;

        ProgrammeLocation location =
                mock(ProgrammeLocation.class);

        when(workoutPersistor.findProgrammeLocation(workoutId))
                .thenReturn(Optional.of(location));

        Optional<ProgrammeLocation> result =
                service.findProgrammeLocation(workoutId);

        assertTrue(result.isPresent());
        assertSame(location, result.get());

        verify(workoutPersistor)
                .findProgrammeLocation(workoutId);
    }

    @Test
    void returnsEmptyProgrammeLocationForStandaloneWorkout() {
        Long workoutId = 10L;

        when(workoutPersistor.findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        Optional<ProgrammeLocation> result =
                service.findProgrammeLocation(workoutId);

        assertTrue(result.isEmpty());

        verify(workoutPersistor)
                .findProgrammeLocation(workoutId);
    }

    @Test
    void deletesStandaloneWorkout() {
        Long workoutId = 10L;

        when(workoutPersistor.findProgrammeLocation(workoutId))
                .thenReturn(Optional.empty());

        service.deleteStandalone(workoutId);

        verify(workoutPersistor)
                .findProgrammeLocation(workoutId);

        verify(workoutPersistor)
                .deleteWorkout(workoutId);
    }

    @Test
    void refusesToDeleteProgrammeOwnedWorkout() {
        Long workoutId = 20L;

        ProgrammeLocation location =
                mock(ProgrammeLocation.class);

        when(workoutPersistor.findProgrammeLocation(workoutId))
                .thenReturn(Optional.of(location));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.deleteStandalone(workoutId)
                );

        assertEquals(
                "A programme workout cannot be deleted independently",
                exception.getMessage()
        );

        verify(workoutPersistor)
                .findProgrammeLocation(workoutId);

        verify(workoutPersistor, never())
                .deleteWorkout(anyLong());
    }

    private Workout mockWorkout(
            Long id,
            String name
    ) {
        Workout workout = mock(Workout.class);

        when(workout.id()).thenReturn(id);
        //when(workout.name()).thenReturn(name);

        return workout;
    }
}