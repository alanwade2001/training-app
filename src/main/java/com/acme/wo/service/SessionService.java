package com.acme.wo.service;

import com.acme.wo.internal.data.DaoService;
import com.acme.wo.model.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
public class SessionService {

    private final DaoService daoService;

    private final WorkoutService workoutService;


    @Transactional
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
        var execution = new SessionExecution( 0, 1, ExecutionState.READY);

        Session session =
                new Session(
                        null,
                        workout.id(),
                        workout.name(),
                        execution,
                        exercises
                );

        return daoService.saveSession( session );
    }




    public void startSet(Long sessionId) {

        Session session = daoService.getSession(sessionId);

        SessionExecution current = session.execution();

        SessionExecution updated =
                nextExecutionAfterStartSet(current);

        daoService.updateSessionExecution(sessionId, updated);
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
                ExecutionState.ACTIVE
        );
    }

    public void completeSet(Long sessionId) {

        // Get the current session, including its current execution
        Session session = daoService.getSession(sessionId);

        SessionExecution current = session.execution();

        // Work out what should happen
        SessionExecution updated =
                nextExecutionAfterSet(session, current);

        // Persist the result
        daoService.updateSessionExecution(sessionId, updated);
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
                    ExecutionState.RESTING
            );
        }

        // That was the final set
        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet(),
                ExecutionState.EXERCISE_COMPLETE
        );
    }

    public void completeRest(Long sessionId) {

        Session session = daoService.getSession(sessionId);

        SessionExecution current = session.execution();

        SessionExecution updated =
                nextExecutionAfterRest(current);

        daoService.updateSessionExecution(
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
                ExecutionState.READY
        );
    }

    @Transactional
    public void nextExercise(Long sessionId) {

        Session session = daoService.getSession(sessionId);

        SessionExecution updated =
                nextExecutionAfterExercise(
                        session,
                        session.execution()
                );

        daoService.updateSessionExecution(
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
                    ExecutionState.READY
            );
        }

        return new SessionExecution(
                current.exerciseIndex(),
                current.currentSet(),
                ExecutionState.SESSION_COMPLETE
        );
    }



    public Session getSessionById(Long sessionId) {
        return daoService.getSession(sessionId);
    }
}
