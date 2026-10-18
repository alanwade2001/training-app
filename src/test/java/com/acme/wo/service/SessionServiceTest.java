package com.acme.wo.service;

import com.acme.wo.internal.data.DaoService;
import com.acme.wo.model.ExecutionState;
import com.acme.wo.internal.workout.HardcodedWorkoutService;
import com.acme.wo.model.Session;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.stream.Stream;

@SpringBootTest(classes = {HardcodedConfig.class})
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private WorkoutService workoutService;

    @Test
    void createSession() {
        var workout = workoutService.findAll().getFirst();
        var session = sessionService.createSession(workout.id());

        Assertions.assertThat(session).isNotNull();
    }

    public static Stream<Arguments> source_findSessionById() {
        return Stream.of(
                Arguments.of((Long)null),
                Arguments.of(-1L)
        );
    }

    @ParameterizedTest()
    @MethodSource("source_findSessionById")
    void findSessionById(Long id) {

        Assertions.assertThatRuntimeException().isThrownBy(() -> sessionService.getSessionById(id));

    }

    void assertSessionState(Long sessionId, ExecutionState state) {
        var session = sessionService.getSessionById(sessionId);
        Assertions.assertThat(session.execution().state()).isEqualByComparingTo(state);
    }

    void assertSessionExecution(
            Long sessionId,
            int exerciseIndex,
            int currentSet,
            ExecutionState state) {

        var session = sessionService.getSessionById(sessionId);

        Assertions.assertThat(session.execution().exerciseIndex())
                .isEqualTo(exerciseIndex);

        Assertions.assertThat(session.execution().currentSet())
                .isEqualTo(currentSet);

        Assertions.assertThat(session.execution().state())
                .isEqualTo(state);
    }



    @Test
    void testHappyPath() {

        var workout = workoutService.findAll().getFirst();

        var session = sessionService.createSession(workout.id());
        assertSessionState(session.id(), ExecutionState.READY);
        assertSessionExecution(session.id(), 0, 1, ExecutionState.READY);

        // Exercise 1 - Dead Lift - Set 1
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);
        assertSessionExecution(
                session.id(), 0, 1, ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.RESTING);
        assertSessionExecution(
                session.id(), 0, 1, ExecutionState.RESTING);

        sessionService.completeRest(session.id());
        assertSessionState(session.id(), ExecutionState.READY);
        assertSessionExecution(
                session.id(), 0, 2, ExecutionState.READY);

        // Exercise 1 - Dead Lift - Set 2
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);
        assertSessionExecution(
                session.id(), 0, 2, ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.EXERCISE_COMPLETE);
        assertSessionExecution(
                session.id(), 0, 2, ExecutionState.EXERCISE_COMPLETE);

        sessionService.nextExercise(session.id());
        assertSessionState(session.id(), ExecutionState.READY);
        assertSessionExecution(
                session.id(), 1, 1, ExecutionState.READY);


        // Exercise 2 - Bulgarian Split Squat - Set 1
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);
        assertSessionExecution(
                session.id(), 1, 1, ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.RESTING);

        sessionService.completeRest(session.id());
        assertSessionState(session.id(), ExecutionState.READY);

        // Exercise 2 - Set 2
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.RESTING);

        sessionService.completeRest(session.id());
        assertSessionState(session.id(), ExecutionState.READY);

        // Exercise 2 - Set 3
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.EXERCISE_COMPLETE);

        sessionService.nextExercise(session.id());
        assertSessionState(session.id(), ExecutionState.READY);


        // Exercise 3 - Romanian Deadlift - Set 1
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.RESTING);

        sessionService.completeRest(session.id());
        assertSessionState(session.id(), ExecutionState.READY);

        // Exercise 3 - Set 2
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.RESTING);

        sessionService.completeRest(session.id());
        assertSessionState(session.id(), ExecutionState.READY);

        // Exercise 3 - Set 3
        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
        assertSessionState(session.id(), ExecutionState.EXERCISE_COMPLETE);

        sessionService.nextExercise(session.id());
        assertSessionState(session.id(), ExecutionState.SESSION_COMPLETE);
    }

}