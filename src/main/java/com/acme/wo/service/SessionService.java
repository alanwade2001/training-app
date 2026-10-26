package com.acme.wo.service;

import com.acme.wo.internal.session.SessionJpaPersistor;
import com.acme.wo.internal.session.SessionPersistor;
import com.acme.wo.model.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Data
@Service
public class SessionService {

    private final SessionPersistor sessionPersistor;

    private final SessionSource sessionSource;

    private final WorkoutService workoutService;


    @Transactional
    public Session createSession(
            Long workoutId) {

        Workout workout =
                workoutService.findById(workoutId);


        List<SessionExercise> exercises =
                workout.exercises()
                        .stream()
                        .map(exercise ->
                                new SessionExercise(
                                        exercise.sequence(),
                                        exercise.name(),
                                        exercise.sets(),
                                        exercise.reps(),
                                        exercise.recoveryInSecs()
                                )
                        )
                        .toList();
        var now = Instant.now();
        var execution = new SessionExecution( 0, 1, now, ExecutionState.READY);

        Session session =
                new Session(
                        null,
                        workout.id(),
                        workout.name(),
                        now,
                        now,
                        execution,
                        exercises
                );

        return sessionPersistor.persist( session );
    }


    private Session findSessionBySessionId(Long sessionId) {
        return sessionSource.getSessions().stream().filter(s->s.id().equals(sessionId)).findFirst().orElse(null);
    }

    public void startSet(Long sessionId) {

        Session session = findSessionBySessionId(sessionId);

        SessionExecution current = session.execution();

        SessionExecution updated =
                nextExecutionAfterStartSet(current);

        sessionPersistor.updateSessionExecution(sessionId, updated);
    }

    private SessionExecution nextExecutionAfterStartSet(
            SessionExecution current) {

        if (current.state() != ExecutionState.READY) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot start set when execution state is %s",
                            current.state()
                    )
            );
        }

        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet(),
                Instant.now(),
                ExecutionState.ACTIVE
        );
    }

    public void completeSet(Long sessionId) {

        // Get the current session, including its current execution
        Session session = findSessionBySessionId(sessionId);

        SessionExecution current = session.execution();

        // Work out what should happen
        SessionExecution updated =
                nextExecutionAfterSet(session, current);

        // Persist the result
        sessionPersistor.updateSessionExecution(sessionId, updated);
    }

    private SessionExecution nextExecutionAfterSet(
            Session session,
            SessionExecution current) {

        // A set can only be completed if it is currently ACTIVE
        if (current.state() != ExecutionState.ACTIVE) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot complete set when execution state is %s",
                            current.state()
                    )
            );
        }

        SessionExercise exercise = session.getCurrentExercise();

        // More sets remain for this exercise
        if (exercise.hasMoreSets(current.currentSet())) {
            return new SessionExecution(
                    current.exerciseIndex(),
                    current.currentSet(),
                    Instant.now(),
                    ExecutionState.RESTING
            );
        }

        // That was the final set
        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet(),
                Instant.now(),
                ExecutionState.EXERCISE_COMPLETE
        );
    }

    public void completeRest(Long sessionId) {

        Session session = findSessionBySessionId(sessionId);

        SessionExecution current = session.execution();

        SessionExecution updated =
                nextExecutionAfterRest(current);

        sessionPersistor.updateSessionExecution(
                sessionId,
                updated
        );
    }

    private SessionExecution nextExecutionAfterRest(
            SessionExecution current) {

        if (current.state() != ExecutionState.RESTING) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot complete rest when execution state is %s",
                            current.state()
                    )
            );
        }

        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet() + 1,
                Instant.now(),
                ExecutionState.READY
        );
    }

    @Transactional
    public void nextExercise(Long sessionId) {

        Session session = findSessionBySessionId(sessionId);

        SessionExecution updated =
                nextExecutionAfterExercise(
                        session,
                        session.execution()
                );

        sessionPersistor.updateSessionExecution(
                sessionId,
                updated
        );
    }

    private SessionExecution nextExecutionAfterExercise(
            Session session,
            SessionExecution current) {

        if (current.state() != ExecutionState.EXERCISE_COMPLETE) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot move to next exercise when execution state is %s",
                            current.state()
                    )
            );
        }

        int nextIndex = current.exerciseIndex() + 1;

        if (session.hasMoreExercises(nextIndex)) {
            return new SessionExecution(
                    nextIndex,
                    1,
                    Instant.now(),
                    ExecutionState.READY
            );
        }

        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet(),
                Instant.now(),
                ExecutionState.SESSION_COMPLETE
        );
    }



    public Session getSessionById(Long sessionId) {
        return findSessionBySessionId(sessionId);
    }

    public List<Session> history() {
        return sessionSource.getSessions().stream()
                .sorted(
                        Comparator.comparing(Session::startedAt)
                                .reversed()
                )
                .toList();
    }

    public Session findById(Long sessionId) {
        return findSessionBySessionId(sessionId);
    }

    public long countByWorkoutId(Long workoutId) {
        return sessionPersistor.findSessionByWorkoutId(workoutId).size();
    }

    public long countCompletedByWorkoutId(Long workoutId) {
        return sessionPersistor.findSessionByWorkoutId(workoutId)
                .stream()
                .filter(s->s.execution().state().isComplete())
                .count();
    }
}
