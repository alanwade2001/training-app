package com.acme.wo.internal.history;

import com.acme.wo.internal.data.SessionEntity;
import com.acme.wo.internal.data.SessionExecutionEntity;
import com.acme.wo.internal.data.SessionExerciseEntity;
import com.acme.wo.model.ExecutionState;
import com.acme.wo.model.Session;
import com.acme.wo.service.SessionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class HistoryIntegrityTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SessionService sessionService;

    @Test
    void incompleteAndCompletedSessionsBothAppearInHistory() {
        Long workoutId = 9_001L;

        SessionEntity incomplete =
                createSession(
                        workoutId,
                        "History Integrity Workout",
                        Instant.parse(
                                "2026-10-30T10:00:00Z"
                        ),
                        ExecutionState.ACTIVE,
                        "300m"
                );

        SessionEntity completed =
                createSession(
                        workoutId,
                        "History Integrity Workout",
                        Instant.parse(
                                "2026-10-30T11:00:00Z"
                        ),
                        ExecutionState.SESSION_COMPLETE,
                        "300m"
                );

        entityManager.persist(incomplete);
        entityManager.persist(completed);
        entityManager.flush();

        Long incompleteId = incomplete.getId();
        Long completedId = completed.getId();

        entityManager.clear();

        List<Session> history =
                sessionService.history();

        Session incompleteHistory =
                findSession(history, incompleteId);

        Session completedHistory =
                findSession(history, completedId);

        assertNotNull(incompleteHistory);
        assertNotNull(completedHistory);

        assertEquals(
                ExecutionState.ACTIVE,
                incompleteHistory.execution().state()
        );

        assertEquals(
                ExecutionState.SESSION_COMPLETE,
                completedHistory.execution().state()
        );

        assertEquals(
                workoutId,
                incompleteHistory.workoutId()
        );

        assertEquals(
                workoutId,
                completedHistory.workoutId()
        );

        assertEquals(
                "History Integrity Workout",
                incompleteHistory.workoutName()
        );

        assertEquals(
                "History Integrity Workout",
                completedHistory.workoutName()
        );

        assertEquals(
                "300m",
                incompleteHistory
                        .exercises()
                        .getFirst()
                        .name()
        );

        assertEquals(
                "300m",
                completedHistory
                        .exercises()
                        .getFirst()
                        .name()
        );

        /*
         * SessionService.history() sorts newest first.
         */
        assertTrue(
                history.indexOf(completedHistory)
                        < history.indexOf(incompleteHistory)
        );
    }

    @Test
    void repeatedWorkoutAttemptsRemainSeparateHistoryEntries() {
        Long workoutId = 9_002L;

        SessionEntity firstAttempt =
                createSession(
                        workoutId,
                        "Repeated Attempt Workout",
                        Instant.parse(
                                "2026-10-30T09:00:00Z"
                        ),
                        ExecutionState.SESSION_COMPLETE,
                        "400m"
                );

        SessionEntity secondAttempt =
                createSession(
                        workoutId,
                        "Repeated Attempt Workout",
                        Instant.parse(
                                "2026-10-30T10:00:00Z"
                        ),
                        ExecutionState.SESSION_COMPLETE,
                        "400m"
                );

        SessionEntity thirdAttempt =
                createSession(
                        workoutId,
                        "Repeated Attempt Workout",
                        Instant.parse(
                                "2026-10-30T11:00:00Z"
                        ),
                        ExecutionState.READY,
                        "400m"
                );

        entityManager.persist(firstAttempt);
        entityManager.persist(secondAttempt);
        entityManager.persist(thirdAttempt);
        entityManager.flush();

        Long firstId = firstAttempt.getId();
        Long secondId = secondAttempt.getId();
        Long thirdId = thirdAttempt.getId();

        entityManager.clear();

        List<Session> attempts =
                sessionService.history()
                        .stream()
                        .filter(session ->
                                workoutId.equals(
                                        session.workoutId()
                                )
                        )
                        .toList();

        assertEquals(3, attempts.size());

        /*
         * Newest-first ordering is preserved.
         */
        assertEquals(
                List.of(thirdId, secondId, firstId),
                attempts.stream()
                        .map(Session::id)
                        .toList()
        );

        /*
         * All attempts belong to the same workout,
         * but remain independent session records.
         */
        assertTrue(
                attempts.stream()
                        .allMatch(session ->
                                workoutId.equals(
                                        session.workoutId()
                                )
                        )
        );

        assertEquals(
                3,
                attempts.stream()
                        .map(Session::id)
                        .distinct()
                        .count()
        );

        assertNotEquals(
                attempts.getFirst().id(),
                attempts.get(1).id()
        );

        assertNotEquals(
                attempts.get(1).id(),
                attempts.get(2).id()
        );

        assertEquals(
                ExecutionState.READY,
                attempts.getFirst()
                        .execution()
                        .state()
        );

        assertEquals(
                ExecutionState.SESSION_COMPLETE,
                attempts.get(1)
                        .execution()
                        .state()
        );

        assertEquals(
                ExecutionState.SESSION_COMPLETE,
                attempts.get(2)
                        .execution()
                        .state()
        );
    }

    private SessionEntity createSession(
            Long workoutId,
            String workoutName,
            Instant startedAt,
            ExecutionState state,
            String exerciseName
    ) {
        SessionExecutionEntity execution =
                SessionExecutionEntity.builder()
                        .exerciseIndex(0)
                        .currentSet(1)
                        .stateStartedAt(startedAt)
                        .state(state)
                        .build();

        SessionEntity session =
                SessionEntity.builder()
                        .workoutId(workoutId)
                        .workoutName(workoutName)
                        .startedAt(startedAt)
                        .updatedAt(startedAt)
                        .currentSessionExecution(execution)
                        .build();

        SessionExerciseEntity exercise =
                SessionExerciseEntity.builder()
                        .sequence(1)
                        .name(exerciseName)
                        .sets(3)
                        .reps(1)
                        .recoveryInSecs(180)
                        .session(session)
                        .build();

        session.setExercises(List.of(exercise));

        return session;
    }

    private Session findSession(
            List<Session> history,
            Long sessionId
    ) {
        return history.stream()
                .filter(session ->
                        sessionId.equals(session.id())
                )
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError(
                                "Session not found in history: "
                                        + sessionId
                        )
                );
    }
}