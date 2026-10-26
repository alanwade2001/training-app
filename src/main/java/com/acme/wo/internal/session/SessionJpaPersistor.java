package com.acme.wo.internal.session;

import com.acme.wo.internal.data.*;
import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;
import com.acme.wo.model.SessionExercise;
import com.acme.wo.service.SessionSource;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Data
@Service
public class SessionJpaPersistor implements SessionPersistor, SessionSource {
    private final SessionRepos sessionRepos;
    private final SessionExerciseRepos sessionExerciseRepos;

    @Override
    public Session persist(Session session) {
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
                .exercises(session.exercises().stream().map(SessionMapper::toEntity).toList())
                .build();

        entity.getExercises().forEach(exercise -> exercise.setSession(entity));

        var savedEntity = sessionRepos.save(entity);

        return SessionMapper.fromEntity(savedEntity);
    }

    private Session getSession(Long sessionId) {

        var session = sessionRepos.findById(sessionId).orElseThrow();
        return SessionMapper.fromEntity(session);
    }

    @Override
    public void updateSessionExecution(Long sessionId, SessionExecution updated) {
        var entity = sessionRepos.findById(sessionId).orElseThrow();

        entity.setUpdatedAt(Instant.now());

        entity.getCurrentSessionExecution().setState(updated.state());
        entity.getCurrentSessionExecution().setCurrentSet(updated.currentSet());
        entity.getCurrentSessionExecution().setExerciseIndex(updated.exerciseIndex());
        entity.getCurrentSessionExecution().setStateStartedAt(updated.stateStartedAt());
        sessionRepos.save(entity);
    }

    @Override
    public List<Session> findSessionByWorkoutId(Long workoutId) {
        return sessionRepos.findByWorkoutId(workoutId).stream().map(SessionMapper::fromEntity).toList();
    }

    @Override
    public List<Session> getSessions() {
        return sessionRepos.findAll().stream().map(SessionMapper::fromEntity).toList();
    }
}
