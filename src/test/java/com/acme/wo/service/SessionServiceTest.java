package com.acme.wo.service;

import com.acme.wo.internal.session.SessionPersistor;
import com.acme.wo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionPersistor sessionPersistor;

    @Mock
    private SessionSource sessionSource;

    @Mock
    private WorkoutService workoutService;

    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(
                sessionPersistor,
                sessionSource,
                workoutService
        );
    }

    @Test
    void createsSessionFromWorkout() {
        Long workoutId = 10L;

        Workout workout = workout(
                workoutId,
                "Speed Endurance",
                List.of(
                        workoutExercise(
                                1,
                                "300m",
                                2,
                                1,
                                300,
                                ExerciseUnit.METERS,
                                180
                        ),
                        workoutExercise(
                                2,
                                "150m",
                                2,
                                1,
                                150,
                                ExerciseUnit.METERS,
                                120
                        )
                )
        );

        Session persistedSession =
                org.mockito.Mockito.mock(Session.class);

        when(workoutService.findById(workoutId))
                .thenReturn(workout);

        when(sessionPersistor.persist(any(Session.class)))
                .thenReturn(persistedSession);

        Instant before = Instant.now();

        Session result = service.createSession(workoutId);

        Instant after = Instant.now();

        assertSame(persistedSession, result);

        ArgumentCaptor<Session> captor =
                ArgumentCaptor.forClass(Session.class);

        verify(sessionPersistor).persist(captor.capture());

        Session created = captor.getValue();

        assertNull(created.id());
        assertEquals(workoutId, created.workoutId());
        assertEquals(
                "Speed Endurance",
                created.workoutName()
        );

        assertTrue(
                !created.startedAt().isBefore(before)
                        && !created.startedAt().isAfter(after)
        );

        assertEquals(
                created.startedAt(),
                created.updatedAt()
        );

        assertEquals(
                0,
                created.execution().exerciseIndex()
        );

        assertEquals(
                1,
                created.execution().currentSet()
        );

        assertEquals(
                ExecutionState.READY,
                created.execution().state()
        );

        assertEquals(
                created.startedAt(),
                created.execution().stateStartedAt()
        );

        assertEquals(2, created.exercises().size());

        SessionExercise first =
                created.exercises().getFirst();

        assertEquals(1, first.sequence());
        assertEquals("300m", first.name());
        assertEquals(2, first.sets());
        assertEquals(1, first.reps());
        assertEquals(180, first.recoveryInSecs());

        SessionExercise second =
                created.exercises().get(1);

        assertEquals(2, second.sequence());
        assertEquals("150m", second.name());
        assertEquals(2, second.sets());
        assertEquals(1, second.reps());
        assertEquals(120, second.recoveryInSecs());
    }

    @Test
    void countsAllSessionsByWorkoutId() {
        Long workoutId = 10L;

        when(sessionPersistor.findSessionByWorkoutId(workoutId))
                .thenReturn(
                        List.of(
                                session(
                                        1L,
                                        workoutId,
                                        ExecutionState.READY
                                ),
                                session(
                                        2L,
                                        workoutId,
                                        ExecutionState.ACTIVE
                                ),
                                session(
                                        3L,
                                        workoutId,
                                        ExecutionState.SESSION_COMPLETE
                                )
                        )
                );

        assertEquals(
                3,
                service.countByWorkoutId(workoutId)
        );
    }

    @Test
    void countsOnlyCompletedSessionsByWorkoutId() {
        Long workoutId = 10L;

        when(sessionPersistor.findSessionByWorkoutId(workoutId))
                .thenReturn(
                        List.of(
                                session(
                                        1L,
                                        workoutId,
                                        ExecutionState.READY
                                ),
                                session(
                                        2L,
                                        workoutId,
                                        ExecutionState.SESSION_COMPLETE
                                ),
                                session(
                                        3L,
                                        workoutId,
                                        ExecutionState.SESSION_COMPLETE
                                )
                        )
                );

        assertEquals(
                2,
                service.countCompletedByWorkoutId(workoutId)
        );
    }

    @Test
    void incompleteSessionCountsAsAttemptButNotCompleted() {
        Long workoutId = 10L;

        Session incomplete =
                session(
                        1L,
                        workoutId,
                        ExecutionState.ACTIVE
                );

        when(sessionPersistor.findSessionByWorkoutId(workoutId))
                .thenReturn(List.of(incomplete));

        assertEquals(
                1,
                service.countByWorkoutId(workoutId)
        );

        assertEquals(
                0,
                service.countCompletedByWorkoutId(workoutId)
        );
    }

    @Test
    void completedSessionCountsAsAttemptAndCompleted() {
        Long workoutId = 10L;

        Session complete =
                session(
                        1L,
                        workoutId,
                        ExecutionState.SESSION_COMPLETE
                );

        when(sessionPersistor.findSessionByWorkoutId(workoutId))
                .thenReturn(List.of(complete));

        assertEquals(
                1,
                service.countByWorkoutId(workoutId)
        );

        assertEquals(
                1,
                service.countCompletedByWorkoutId(workoutId)
        );
    }

    @Test
    void startsReadySet() {
        Long sessionId = 1L;

        Session current =
                session(
                        sessionId,
                        10L,
                        ExecutionState.READY
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        Instant before = Instant.now();

        service.startSet(sessionId);

        Instant after = Instant.now();

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(0, updated.exerciseIndex());
        assertEquals(1, updated.currentSet());

        assertEquals(
                ExecutionState.ACTIVE,
                updated.state()
        );

        assertTrue(
                !updated.stateStartedAt().isBefore(before)
                        && !updated.stateStartedAt().isAfter(after)
        );
    }

    @Test
    void refusesToStartSetUnlessReady() {
        Long sessionId = 1L;

        Session current =
                session(
                        sessionId,
                        10L,
                        ExecutionState.ACTIVE
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        assertThrows(
                IllegalStateException.class,
                () -> service.startSet(sessionId)
        );

        verify(sessionPersistor, never())
                .updateSessionExecution(
                        any(),
                        any()
                );
    }

    @Test
    void completingSetStartsRestWhenMoreSetsRemain() {
        Long sessionId = 1L;

        Session current =
                sessionWithExercises(
                        sessionId,
                        ExecutionState.ACTIVE,
                        0,
                        1,
                        List.of(
                                new SessionExercise(
                                        1,
                                        "300m",
                                        2,
                                        1,
                                        180
                                )
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        service.completeSet(sessionId);

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(0, updated.exerciseIndex());
        assertEquals(1, updated.currentSet());

        assertEquals(
                ExecutionState.RESTING,
                updated.state()
        );
    }

    @Test
    void completingFinalSetCompletesExercise() {
        Long sessionId = 1L;

        Session current =
                sessionWithExercises(
                        sessionId,
                        ExecutionState.ACTIVE,
                        0,
                        2,
                        List.of(
                                new SessionExercise(
                                        1,
                                        "300m",
                                        2,
                                        1,
                                        180
                                )
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        service.completeSet(sessionId);

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(
                ExecutionState.EXERCISE_COMPLETE,
                updated.state()
        );
    }

    @Test
    void refusesToCompleteSetUnlessActive() {
        Long sessionId = 1L;

        Session current =
                session(
                        sessionId,
                        10L,
                        ExecutionState.READY
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        assertThrows(
                IllegalStateException.class,
                () -> service.completeSet(sessionId)
        );

        verify(sessionPersistor, never())
                .updateSessionExecution(
                        any(),
                        any()
                );
    }

    @Test
    void completingRestMovesToNextSet() {
        Long sessionId = 1L;

        Session current =
                sessionWithExercises(
                        sessionId,
                        ExecutionState.RESTING,
                        0,
                        1,
                        List.of(
                                new SessionExercise(
                                        1,
                                        "300m",
                                        2,
                                        1,
                                        180
                                )
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        service.completeRest(sessionId);

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(0, updated.exerciseIndex());
        assertEquals(2, updated.currentSet());

        assertEquals(
                ExecutionState.READY,
                updated.state()
        );
    }

    @Test
    void refusesToCompleteRestUnlessResting() {
        Long sessionId = 1L;

        Session current =
                session(
                        sessionId,
                        10L,
                        ExecutionState.ACTIVE
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        assertThrows(
                IllegalStateException.class,
                () -> service.completeRest(sessionId)
        );

        verify(sessionPersistor, never())
                .updateSessionExecution(
                        any(),
                        any()
                );
    }

    @Test
    void movesToNextExerciseWhenAnotherExerciseExists() {
        Long sessionId = 1L;

        Session current =
                sessionWithExercises(
                        sessionId,
                        ExecutionState.EXERCISE_COMPLETE,
                        0,
                        2,
                        List.of(
                                new SessionExercise(
                                        1,
                                        "300m",
                                        2,
                                        1,
                                        180
                                ),
                                new SessionExercise(
                                        2,
                                        "150m",
                                        2,
                                        1,
                                        120
                                )
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        service.nextExercise(sessionId);

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(1, updated.exerciseIndex());
        assertEquals(1, updated.currentSet());

        assertEquals(
                ExecutionState.READY,
                updated.state()
        );
    }

    @Test
    void completesSessionAfterFinalExercise() {
        Long sessionId = 1L;

        Session current =
                sessionWithExercises(
                        sessionId,
                        ExecutionState.EXERCISE_COMPLETE,
                        0,
                        2,
                        List.of(
                                new SessionExercise(
                                        1,
                                        "300m",
                                        2,
                                        1,
                                        180
                                )
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        service.nextExercise(sessionId);

        SessionExecution updated =
                captureUpdatedExecution(sessionId);

        assertEquals(
                ExecutionState.SESSION_COMPLETE,
                updated.state()
        );
    }

    @Test
    void refusesToMoveToNextExerciseUnlessExerciseComplete() {
        Long sessionId = 1L;

        Session current =
                session(
                        sessionId,
                        10L,
                        ExecutionState.ACTIVE
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(current));

        assertThrows(
                IllegalStateException.class,
                () -> service.nextExercise(sessionId)
        );

        verify(sessionPersistor, never())
                .updateSessionExecution(
                        any(),
                        any()
                );
    }

    @Test
    void findsHistoricalSessionById() {
        Session expected =
                session(
                        2L,
                        10L,
                        ExecutionState.SESSION_COMPLETE
                );

        when(sessionSource.getSessions())
                .thenReturn(List.of(expected));

        assertSame(
                expected,
                service.getSessionById(2L)
        );

        assertSame(
                expected,
                service.findById(2L)
        );
    }

    @Test
    void returnsNullWhenHistoricalSessionDoesNotExist() {
        when(sessionSource.getSessions())
                .thenReturn(List.of());

        assertNull(service.getSessionById(99L));
        assertNull(service.findById(99L));
    }

    @Test
    void returnsHistoryNewestFirst() {
        Session oldest =
                sessionAt(
                        1L,
                        Instant.parse(
                                "2026-10-28T10:00:00Z"
                        )
                );

        Session newest =
                sessionAt(
                        3L,
                        Instant.parse(
                                "2026-10-30T10:00:00Z"
                        )
                );

        Session middle =
                sessionAt(
                        2L,
                        Instant.parse(
                                "2026-10-29T10:00:00Z"
                        )
                );

        when(sessionSource.getSessions())
                .thenReturn(
                        List.of(oldest, newest, middle)
                );

        List<Session> result = service.history();

        assertEquals(
                List.of(3L, 2L, 1L),
                result.stream()
                        .map(Session::id)
                        .toList()
        );
    }

    private SessionExecution captureUpdatedExecution(
            Long sessionId
    ) {
        ArgumentCaptor<SessionExecution> captor =
                ArgumentCaptor.forClass(
                        SessionExecution.class
                );

        verify(sessionPersistor)
                .updateSessionExecution(
                        org.mockito.ArgumentMatchers.eq(
                                sessionId
                        ),
                        captor.capture()
                );

        return captor.getValue();
    }

    private Workout workout(
            Long id,
            String name,
            List<WorkoutExercise> exercises
    ) {
        return new Workout(
                id,
                name,
                null,
                exercises
        );
    }

    private WorkoutExercise workoutExercise(
            int sequence,
            String name,
            int sets,
            int reps,
            int quantity,
            ExerciseUnit units,
            int recoveryInSecs
    ) {
        return new WorkoutExercise(
                sequence,
                name,
                sets,
                reps,
                quantity,
                units,
                recoveryInSecs
        );
    }

    private Session session(
            Long id,
            Long workoutId,
            ExecutionState state
    ) {
        return sessionWithExercises(
                id,
                state,
                0,
                1,
                List.of(
                        new SessionExercise(
                                1,
                                "300m",
                                2,
                                1,
                                180
                        )
                ),
                workoutId
        );
    }

    private Session sessionWithExercises(
            Long id,
            ExecutionState state,
            int exerciseIndex,
            int currentSet,
            List<SessionExercise> exercises
    ) {
        return sessionWithExercises(
                id,
                state,
                exerciseIndex,
                currentSet,
                exercises,
                10L
        );
    }

    private Session sessionWithExercises(
            Long id,
            ExecutionState state,
            int exerciseIndex,
            int currentSet,
            List<SessionExercise> exercises,
            Long workoutId
    ) {
        Instant startedAt =
                Instant.parse("2026-10-30T10:00:00Z");

        return new Session(
                id,
                workoutId,
                "Speed Endurance",
                startedAt,
                startedAt,
                new SessionExecution(
                        exerciseIndex,
                        currentSet,
                        startedAt,
                        state
                ),
                exercises
        );
    }

    private Session sessionAt(
            Long id,
            Instant startedAt
    ) {
        return new Session(
                id,
                10L,
                "Speed Endurance",
                startedAt,
                startedAt,
                new SessionExecution(
                        0,
                        1,
                        startedAt,
                        ExecutionState.SESSION_COMPLETE
                ),
                List.of()
        );
    }
}