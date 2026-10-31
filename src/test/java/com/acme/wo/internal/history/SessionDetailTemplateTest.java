package com.acme.wo.internal.history;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SessionDetailTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     SessionDetailTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/session-detail.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/session-detail.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void rendersHistoricalWorkoutName() {
        Element heading =
                document.selectFirst(".page-title");

        assertNotNull(
                heading,
                "Expected historical workout name"
        );

        assertEquals(
                "${trainingsession.workoutName}",
                heading.attr("th:text")
        );
    }

    @Test
    void rendersStartedAtAndUpdatedAt() {
        assertTrue(
                containsThymeleafExpression(
                        "th:text",
                        "trainingsession.startedAt"
                ),
                "Expected startedAt to be rendered"
        );

        assertTrue(
                containsThymeleafExpression(
                        "th:text",
                        "trainingsession.updatedAt"
                ),
                "Expected updatedAt to be rendered"
        );
    }

    @Test
    void rendersPersistedSessionExerciseSnapshots() {
        Element exercises =
                document.selectFirst(
                        ".history-exercises"
                );

        assertNotNull(
                exercises,
                "Expected historical exercises section"
        );

        assertEquals(
                "${!#lists.isEmpty(trainingsession.exercises)}",
                exercises.attr("th:if")
        );

        Element exercise =
                document.selectFirst(
                        ".history-exercise-card"
                );

        assertNotNull(
                exercise,
                "Expected historical exercise card"
        );

        assertEquals(
                "exercise : ${trainingsession.exercises}",
                exercise.attr("th:each")
        );

        Element sequence =
                exercise.selectFirst(
                        ".history-exercise-sequence"
                );

        assertNotNull(sequence);

        assertTrue(
                sequence.attr("th:text")
                        .contains("exercise.sequence"),
                "Expected persisted exercise sequence"
        );

        Element name =
                exercise.selectFirst(
                        ".history-exercise-name"
                );

        assertNotNull(name);

        assertEquals(
                "${exercise.name}",
                name.attr("th:text")
        );
    }

    @Test
    void rendersSetsRepsAndRecovery() {
        Element exercise =
                document.selectFirst(
                        ".history-exercise-card"
                );

        assertNotNull(exercise);

        assertTrue(
                containsThymeleafExpression(
                        exercise,
                        "th:text",
                        "exercise.sets"
                ),
                "Expected exercise sets"
        );

        assertTrue(
                containsThymeleafExpression(
                        exercise,
                        "th:text",
                        "exercise.reps"
                ),
                "Expected exercise reps"
        );

        assertTrue(
                containsThymeleafExpression(
                        exercise,
                        "th:text",
                        "exercise.recoveryInSecs"
                ),
                "Expected exercise recovery"
        );

        Element recovery =
                exercise
                        .getAllElements()
                        .stream()
                        .filter(element ->
                                element.hasAttr("th:if")
                        )
                        .filter(element ->
                                normalise(
                                        element.attr("th:if")
                                ).contains(
                                        "exercise.recoveryInSecs > 0"
                                )
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(
                recovery,
                """
                Expected recovery to be conditional on
                recoveryInSecs > 0
                """
        );
    }

    @Test
    void emptyStateDisplaysOnlyWhenNoSessionExercisesExist() {
        Element exerciseSection =
                document.selectFirst(
                        ".history-exercises"
                );

        Element emptyState =
                document.selectFirst(
                        ".empty-state"
                );

        assertNotNull(
                exerciseSection,
                "Expected exercise section"
        );

        assertNotNull(
                emptyState,
                "Expected empty state"
        );

        assertEquals(
                "${!#lists.isEmpty(trainingsession.exercises)}",
                exerciseSection.attr("th:if")
        );

        assertEquals(
                "${#lists.isEmpty(trainingsession.exercises)}",
                emptyState.attr("th:if")
        );
    }

    private static boolean containsThymeleafExpression(
            String attribute,
            String expression
    ) {
        return containsThymeleafExpression(
                document,
                attribute,
                expression
        );
    }

    private static boolean containsThymeleafExpression(
            Element root,
            String attribute,
            String expression
    ) {
        return root
                .getAllElements()
                .stream()
                .filter(element ->
                        element.hasAttr(attribute)
                )
                .map(element ->
                        normalise(
                                element.attr(attribute)
                        )
                )
                .anyMatch(value ->
                        value.contains(expression)
                );
    }

    private static String normalise(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}