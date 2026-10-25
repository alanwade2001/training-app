package com.acme.wo.service;

import com.acme.wo.model.ExecutionState;
import com.acme.wo.model.Session;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    public void executeExerciseSet(Session session, int exerciseIndex, int currentSet) {

        assertSessionState(session.id(), ExecutionState.READY);
        assertSessionExecution(session.id(), exerciseIndex, currentSet, ExecutionState.READY);

        sessionService.startSet(session.id());
        assertSessionState(session.id(), ExecutionState.ACTIVE);
        assertSessionExecution(
                session.id(), exerciseIndex, currentSet, ExecutionState.ACTIVE);

        sessionService.completeSet(session.id());
    }

    public void executeRest(Session session, int exerciseIndex, int currentSet) {
        assertSessionState(session.id(), ExecutionState.RESTING);

        assertSessionExecution(
                session.id(), exerciseIndex, currentSet, ExecutionState.RESTING);

        sessionService.completeRest(session.id());
    }



    @Test
    void testHappyPath() {

        String name = "Force & Reactive - Test";
        var workout = workoutService.findByName(name);


        var session = sessionService.createSession(workout.id());
        //////////////////////////////////
        // Exercise 1 - Pogos - Set 1/2
        executeExerciseSet(session, 0, 1);
        executeRest(session, 0, 1);
        // Exercise 1 - Pogos - Set 2/2
        executeExerciseSet(session, 0, 2);
        //////////////////////

        sessionService.nextExercise(session.id());

        /////////////////////////////
        // Exercise 2 - Dead Lift - Set 1/3
        executeExerciseSet(session, 1, 1);
        executeRest(session, 1, 1);
        // Exercise 2 - Dead Lift - Set 2/3
        executeExerciseSet(session, 1, 2);
        executeRest(session, 1, 2);
        // Exercise 2 - Dead Lift - Set 3/3
        executeExerciseSet(session, 1, 3);
        /// ///////////////////////////////
        sessionService.nextExercise(session.id());

        /// ////////////////////////////////
        // Exercise 3 - Bulgarian Split Squat - Set 1/2
        executeExerciseSet(session, 2, 1);
        executeRest(session, 2, 1);
        // Exercise 3 - Bulgarian Split Squat - Set 2/2
        executeExerciseSet(session, 2, 2);
        /// /////////////////////////////

        sessionService.nextExercise(session.id());

        // Exercise 4 - Romanian Deadlift - Set 1/2
        executeExerciseSet(session, 3, 1);
        executeRest(session, 3, 1);
        // Exercise 4 - Romanian Deadlift - Set 2/2
        executeExerciseSet(session, 3, 2);
        /// /////////////////////////////

        sessionService.nextExercise(session.id());

        /// /////////////////////////
        // Exercise 5 - Standing calf raise - Set 1/2
        executeExerciseSet(session, 4, 1);
        executeRest(session, 4, 1);
        // Exercise 5 - Standing calf raise - Set 2/2
        executeExerciseSet(session, 4, 2);
        /// /////////////////////////////

        sessionService.nextExercise(session.id());

        /// /////////////////////////////
        // Exercise 6 - Pallof Press - each side - Set 1/2
        executeExerciseSet(session, 5, 1);
        executeRest(session, 5, 1);
        // Exercise 6 - Pallof Press - each side - Set 2/2
        executeExerciseSet(session, 5, 2);
        executeRest(session, 5, 2);
        // Exercise 6 - Pallof Press - each side - Set 2/2
        executeExerciseSet(session, 5, 3);
        /// /////////////////////////////

        sessionService.nextExercise(session.id());
        assertSessionState(session.id(), ExecutionState.SESSION_COMPLETE);
    }

}