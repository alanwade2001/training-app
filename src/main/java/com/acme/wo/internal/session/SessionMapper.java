package com.acme.wo.internal.session;

import com.acme.wo.internal.data.SessionEntity;
import com.acme.wo.internal.data.SessionExecutionEntity;
import com.acme.wo.internal.data.SessionExerciseEntity;
import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;
import com.acme.wo.model.SessionExercise;

import java.time.Instant;

public class SessionMapper {

    public static SessionEntity toEntity(Session session) {
        var entity = SessionEntity.builder()
                .workoutId(session.workoutId())
                .workoutName(session.workoutName())
                .startedAt(Instant.now())
                .updatedAt(Instant.now())
                .currentSessionExecution(SessionExecutionEntity.builder()
                        .currentSet(session.execution().currentSet())
                        .exerciseIndex(session.execution().exerciseIndex())
                        .state(session.execution().state())
                        .stateStartedAt(session.execution().stateStartedAt())
                        .build()
                )
                .build();

        var exercises = session.exercises().stream()
                .map(SessionMapper::toEntity)
                .toList();

        exercises.forEach(e->e.setSession(entity));

        entity.setExercises(exercises);

        return entity;
    }

    public static SessionExerciseEntity toEntity(SessionExercise sessionExercise) {
        return SessionExerciseEntity.builder()
                .reps(sessionExercise.reps())
                .sets(sessionExercise.sets())
                .name(sessionExercise.name())
                .sequence(sessionExercise.sequence())
                .recoveryInSecs(sessionExercise.recoveryInSecs())
                .build();


    }

    public static Session fromEntity( SessionEntity entity) {
        return new Session(entity.getId()
                , entity.getWorkoutId()
                , entity.getWorkoutName()
                , entity.getStartedAt()
                , entity.getUpdatedAt()
                , fromEntity(entity.getCurrentSessionExecution())
                , entity.getExercises().stream().map(SessionMapper::fromEntity).toList()
        );
    }

    public static SessionExercise fromEntity( SessionExerciseEntity entity) {
        return new SessionExercise(entity.getSequence()
                , entity.getName()
                , entity.getSets()
                , entity.getReps()
                , entity.getRecoveryInSecs()
                );
    }

    public static SessionExecution fromEntity(SessionExecutionEntity entity) {
        return new SessionExecution(entity.getExerciseIndex()
                , entity.getCurrentSet()
                , entity.getStateStartedAt()
                , entity.getState());
    }
}
