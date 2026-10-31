package com.acme.wo.internal.session;

import com.acme.wo.internal.data.SessionEntity;
import com.acme.wo.internal.data.SessionExecutionEntity;
import com.acme.wo.internal.data.SessionExerciseEntity;
import com.acme.wo.model.ExecutionState;
import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;
import com.acme.wo.model.SessionExercise;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionMapperTest {

    @Test
    void mapsSessionToEntity() {
        Instant stateStartedAt = Instant.parse("2026-10-30T10:00:00Z");

        SessionExecution execution = new SessionExecution(
                0,
                2,
                stateStartedAt,
                ExecutionState.ACTIVE
        );

        SessionExercise exercise = new SessionExercise(
                1,
                "400m",
                3,
                2,
                180
        );

        Session session = new Session(
                1L,
                10L,
                "Speed Endurance",
                Instant.parse("2026-10-30T09:55:00Z"),
                Instant.parse("2026-10-30T09:56:00Z"),
                execution,
                List.of(exercise)
        );

        SessionEntity entity = SessionMapper.toEntity(session);

        assertEquals(10L, entity.getWorkoutId());
        assertEquals("Speed Endurance", entity.getWorkoutName());

        assertNotNull(entity.getStartedAt());
        assertNotNull(entity.getUpdatedAt());

        assertNotNull(entity.getCurrentSessionExecution());
        assertEquals(
                0,
                entity.getCurrentSessionExecution().getExerciseIndex()
        );
        assertEquals(
                2,
                entity.getCurrentSessionExecution().getCurrentSet()
        );
        assertEquals(
                stateStartedAt,
                entity.getCurrentSessionExecution().getStateStartedAt()
        );
        assertEquals(
                ExecutionState.ACTIVE,
                entity.getCurrentSessionExecution().getState()
        );

        assertEquals(1, entity.getExercises().size());

        SessionExerciseEntity mappedExercise =
                entity.getExercises().getFirst();

        assertEquals(1, mappedExercise.getSequence());
        assertEquals("400m", mappedExercise.getName());
        assertEquals(3, mappedExercise.getSets());
        assertEquals(2, mappedExercise.getReps());
        assertEquals(180, mappedExercise.getRecoveryInSecs());

        assertSame(entity, mappedExercise.getSession());
    }

    @Test
    void mapsEntityToSession() {
        Instant startedAt =
                Instant.parse("2026-10-30T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-30T10:05:00Z");

        Instant stateStartedAt =
                Instant.parse("2026-10-30T10:01:00Z");

        SessionExecutionEntity execution =
                SessionExecutionEntity.builder()
                        .exerciseIndex(0)
                        .currentSet(2)
                        .stateStartedAt(stateStartedAt)
                        .state(ExecutionState.ACTIVE)
                        .build();

        SessionEntity entity =
                SessionEntity.builder()
                        .workoutId(10L)
                        .workoutName("Speed Endurance")
                        .startedAt(startedAt)
                        .updatedAt(updatedAt)
                        .currentSessionExecution(execution)
                        .build();

        SessionExerciseEntity exercise =
                SessionExerciseEntity.builder()
                        .sequence(1)
                        .name("400m")
                        .sets(3)
                        .reps(2)
                        .recoveryInSecs(180)
                        .session(entity)
                        .build();

        entity.setExercises(List.of(exercise));

        Session session = SessionMapper.fromEntity(entity);

        assertEquals(10L, session.workoutId());
        assertEquals("Speed Endurance", session.workoutName());
        assertEquals(startedAt, session.startedAt());
        assertEquals(updatedAt, session.updatedAt());

        assertEquals(0, session.execution().exerciseIndex());
        assertEquals(2, session.execution().currentSet());
        assertEquals(
                stateStartedAt,
                session.execution().stateStartedAt()
        );
        assertEquals(
                ExecutionState.ACTIVE,
                session.execution().state()
        );

        assertEquals(1, session.exercises().size());

        SessionExercise mappedExercise =
                session.exercises().getFirst();

        assertEquals(1, mappedExercise.sequence());
        assertEquals("400m", mappedExercise.name());
        assertEquals(3, mappedExercise.sets());
        assertEquals(2, mappedExercise.reps());
        assertEquals(180, mappedExercise.recoveryInSecs());
    }
}