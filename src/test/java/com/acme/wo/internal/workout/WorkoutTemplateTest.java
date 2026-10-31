package com.acme.wo.internal.workout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     WorkoutTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/workout.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/workout.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void rendersWorkoutName() {
        Element heading = document.selectFirst("h1");

        assertNotNull(
                heading,
                "Expected workout heading"
        );

        assertEquals(
                "${workout.name}",
                heading.attr("th:text")
        );
    }

    @Test
    void rendersEveryExercise() {
        Element exercise =
                document.selectFirst(".exercise-card");

        assertNotNull(
                exercise,
                "Expected exercise card"
        );

        assertEquals(
                "exercise, status : ${workout.exercises}",
                normalise(exercise.attr("th:each"))
        );
    }

    @Test
    void rendersExercisesInSequence() {
        Element exerciseNumber =
                document.selectFirst(".exercise-number");

        assertNotNull(
                exerciseNumber,
                "Expected exercise sequence number"
        );

        assertEquals(
                "${status.count}",
                exerciseNumber.attr("th:text")
        );
    }

    @Test
    void rendersSetsAndReps() {
        Element exercise =
                document.selectFirst(".exercise-card");

        assertNotNull(exercise);

        assertTrue(
                containsThymeleafAttribute(
                        exercise,
                        "th:text",
                        "${exercise.sets}"
                ),
                "Expected exercise sets"
        );

        assertTrue(
                containsThymeleafAttribute(
                        exercise,
                        "th:text",
                        "${exercise.reps}"
                ),
                "Expected exercise reps"
        );
    }

    @Test
    void rendersQuantityAndUnits() {
        Element exercise =
                document.selectFirst(".exercise-card");

        assertNotNull(exercise);

        assertTrue(
                containsThymeleafAttribute(
                        exercise,
                        "th:text",
                        "${exercise.quantity}"
                ),
                "Expected exercise quantity"
        );

        assertTrue(
                containsThymeleafAttribute(
                        exercise,
                        "th:text",
                        "${exercise.units}"
                ),
                "Expected exercise units"
        );
    }

    @Test
    void rendersRecovery() {
        Element exercise =
                document.selectFirst(".exercise-card");

        assertNotNull(exercise);

        assertTrue(
                containsThymeleafAttribute(
                        exercise,
                        "th:text",
                        "${exercise.recoveryInSecs}"
                ),
                "Expected exercise recovery"
        );
    }

    @Test
    void startWorkoutPostsWorkoutIdToSessions() {
        Element startForm =
                document.select(".workout-preview-actions form")
                        .first();

        assertNotNull(
                startForm,
                "Expected Start workout form"
        );

        assertEquals(
                "@{/sessions}",
                startForm.attr("th:action")
        );

        assertEquals(
                "post",
                startForm.attr("method")
        );

        Element workoutId =
                startForm.selectFirst(
                        "input[type=hidden][name=workoutId]"
                );

        assertNotNull(
                workoutId,
                "Expected hidden workoutId"
        );

        assertEquals(
                "${workout.id}",
                workoutId.attr("th:value")
        );
    }

    @Test
    void backLinkUsesControllerProvidedReturnUrl() {
        Element back =
                document.selectFirst(".back-link");

        assertNotNull(
                back,
                "Expected Back link"
        );

        assertEquals(
                "${returnUrl}",
                back.attr("th:href")
        );
    }

    @Test
    void standaloneWorkoutShowsDeleteAction() {
        Element deleteForm =
                document.selectFirst(".delete-form");

        assertNotNull(
                deleteForm,
                "Expected delete form"
        );

        assertEquals(
                "${standalone}",
                deleteForm.attr("th:if")
        );

        assertEquals(
                "post",
                deleteForm.attr("method")
        );

        assertTrue(
                deleteForm
                        .attr("th:action")
                        .contains(
                                "/workouts/{workoutId}/delete"
                        ),
                "Expected standalone workout delete endpoint"
        );

        assertTrue(
                deleteForm
                        .attr("th:action")
                        .contains(
                                "workoutId=${workout.id}"
                        ),
                "Expected workout id in delete action"
        );
    }

    @Test
    void programmeOwnedWorkoutHidesStandaloneDeleteAction() {
        Element deleteForm =
                document.selectFirst(".delete-form");

        assertNotNull(deleteForm);

        /*
         * The form exists in the source template but Thymeleaf
         * only renders it when standalone == true.
         *
         * Therefore a programme-owned workout
         * (standalone == false) will not get the delete form.
         */
        assertEquals(
                "${standalone}",
                deleteForm.attr("th:if")
        );
    }

    private static boolean containsThymeleafAttribute(
            Element root,
            String attribute,
            String value
    ) {
        return root
                .getAllElements()
                .stream()
                .anyMatch(element ->
                        value.equals(
                                element.attr(attribute)
                        )
                );
    }

    private static String normalise(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}