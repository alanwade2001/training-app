package com.acme.wo.internal.data;

import com.acme.wo.model.ExecutionState;
import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;
import com.acme.wo.model.SessionExercise;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class DaoService {
    private final SessionRepos sessionRepos;
    private final SessionExerciseRepos sessionExerciseRepos;

    public Session saveSession(Session session) {
        var entity = SessionEntity.builder()
                .workoutId(session.workoutName())
                .workoutName(session.workoutName())
                .currentSessionExecution(SessionExecutionEntity.builder()
                        .currentSet(session.execution().currentSet())
                        .exerciseIndex(session.execution().exerciseIndex())
                        .state(session.execution().state())
                        .stateStartedAt(session.execution().stateStartedAt())
                        .build()
                )
                .build();

        var savedEntity = sessionRepos.save(entity);
        var exercises = session.exercises().stream()
                .map(e->saveSessionExercise(savedEntity, e))
                .toList();

        var execution = new SessionExecution(savedEntity.getCurrentSessionExecution().getExerciseIndex(),
                savedEntity.getCurrentSessionExecution().getCurrentSet(),
                savedEntity.getCurrentSessionExecution().getStateStartedAt(),
                savedEntity.getCurrentSessionExecution().getState()
        );

        return new Session(entity.getId(), entity.getWorkoutId(), entity.getWorkoutName(), execution, exercises);
    }

    public SessionExercise saveSessionExercise(SessionEntity session, SessionExercise sessionExercise) {
        var entity = SessionExerciseEntity.builder()
                .session(session)
                .reps(sessionExercise.reps())
                .sets(sessionExercise.sets())
                .name(sessionExercise.name())
                .sequence(sessionExercise.sequence())
                .recoveryInSecs(sessionExercise.recoveryInSecs())
                .build();

        var savedEntity = sessionExerciseRepos.save(entity);

        return new SessionExercise(savedEntity.getSequence()
                , savedEntity.getName()
                , savedEntity.getSets()
                , savedEntity.getReps()
                , savedEntity.getRecoveryInSecs());
    }

    public Session getSession(Long sessionId) {

        var session = sessionRepos.findById(sessionId).orElseThrow();
        var exercises = sessionExerciseRepos.findAllBySessionId(sessionId);
        var execution = session.getCurrentSessionExecution();

        var exerciseModels = exercises.stream().map(e->{
            return new SessionExercise(e.sequence(), e.name(), e.sets(), e.reps(), e.recoveryInSecs());
        }).toList();

        var executionModel = new SessionExecution(execution.exerciseIndex, execution.currentSet, execution.stateStartedAt, execution.state);

        return new Session(session.getId()
                , session.getWorkoutId()
                , session.getWorkoutName()
                , executionModel
                , exerciseModels);
    }

    public void updateSessionExecution(Long sessionId, SessionExecution updated) {
        var entity = sessionRepos.findById(sessionId).orElseThrow();
        entity.getCurrentSessionExecution().setState(updated.state());
        entity.getCurrentSessionExecution().setCurrentSet(updated.currentSet());
        entity.getCurrentSessionExecution().setExerciseIndex(updated.exerciseIndex());
        entity.getCurrentSessionExecution().setStateStartedAt(updated.stateStartedAt());
        sessionRepos.save(entity);
    }
}
