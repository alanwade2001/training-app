package com.acme.wo;

import com.acme.wo.model.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionService {

    private final WorkoutService workoutService;

    private final Map<Long, Session> sessions =
            new HashMap<>();

    private final Map<Long, SessionExecution> executions =
            new HashMap<>();

    private long nextId = 1;


    public SessionService(
            WorkoutService workoutService) {

        this.workoutService = workoutService;
    }


    public Session createSession(
            String workoutId) {

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


        Session session =
                new Session(
                        nextId++,
                        workout.id(),
                        workout.name(),
                        exercises
                );

        SessionExecution execution =
                new SessionExecution(
                        session.id(),
                        0,
                        1,
                        ExecutionState.READY);


        sessions.put(
                session.id(),
                session
        );

        executions.put(
                session.id(),
                execution
        );


        return session;
    }


    public Session findById(Long sessionId) {

        Session session =
                sessions.get(sessionId);

        if (session == null) {
            throw new IllegalArgumentException(
                    "Session not found: "
                            + sessionId
            );
        }

        return session;
    }

    public SessionExecution execution(
            Long sessionId) {

        SessionExecution execution =
                executions.get(
                        sessionId
                );

        if (execution == null) {

            throw new IllegalArgumentException(
                    "Session execution not found: "
                            + sessionId
            );
        }

        return execution;
    }


    public SessionExercise currentExercise(
            Long sessionId) {

        Session session =
                findById(
                        sessionId
                );

        SessionExecution execution =
                execution(
                        sessionId
                );


        return session.exercises()
                .get(
                        execution.exerciseIndex()
                );
    }


    public void startSet(
            Long sessionId) {

        SessionExecution current =
                execution(
                        sessionId
                );


        SessionExecution updated =
                new SessionExecution(
                        current.sessionId(),
                        current.exerciseIndex(),
                        current.currentSet(),
                        ExecutionState.ACTIVE
                );


        executions.put(
                sessionId,
                updated
        );
    }

    public void completeSet(Long sessionId) {

        SessionExecution current =
                execution(sessionId);

        SessionExercise exercise =
                currentExercise(sessionId);


        ExecutionState nextState;

        if (current.currentSet() < exercise.sets()) {

            nextState =
                    ExecutionState.RESTING;

        } else {

            nextState =
                    ExecutionState.EXERCISE_COMPLETE;
        }


        SessionExecution updated =
                new SessionExecution(
                        current.sessionId(),
                        current.exerciseIndex(),
                        current.currentSet(),
                        nextState
                );


        executions.put(
                sessionId,
                updated
        );
    }

    public void completeRest(Long sessionId) {

        SessionExecution current =
                execution(sessionId);

        SessionExecution updated =
                new SessionExecution(
                        current.sessionId(),
                        current.exerciseIndex(),
                        current.currentSet() + 1,
                        ExecutionState.READY
                );

        executions.put(
                sessionId,
                updated
        );
    }

    public void nextExercise(Long sessionId) {

        Session session = findById(sessionId);

        SessionExecution current =
                execution(sessionId);

        int nextIndex =
                current.exerciseIndex() + 1;


        SessionExecution updated;


        if (nextIndex >= session.exercises().size()) {

            updated =
                    new SessionExecution(
                            current.sessionId(),
                            current.exerciseIndex(),
                            current.currentSet(),
                            ExecutionState.SESSION_COMPLETE
                    );

        } else {

            updated = new SessionExecution(
                    current.sessionId(),
                    nextIndex,
                    1,
                    ExecutionState.READY
            );
        }

        executions.put(sessionId, updated);
    }
}
