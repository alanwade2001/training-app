package com.acme.wo.internal.programme;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class WeekTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     WeekTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/week.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/week.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void rendersProgrammeNameAndWeekNumber() {
        Element programmeName =
                document.selectFirst(".programme-name");

        assertNotNull(
                programmeName,
                "Expected programme name"
        );

        assertEquals(
                "${programme.name}",
                programmeName.attr("th:text")
        );

        Element pageTitle =
                document.selectFirst(".page-title");

        assertNotNull(
                pageTitle,
                "Expected week page title"
        );

        Element weekNumber =
                pageTitle.getAllElements()
                        .stream()
                        .filter(element ->
                                "${week.number}".equals(
                                        element.attr("th:text")
                                )
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(
                weekNumber,
                "Expected week number"
        );
    }

    @Test
    void rendersWorkoutProgressItems() {
        Element workoutCard =
                document.selectFirst(".workout-card");

        assertNotNull(
                workoutCard,
                "Expected workout card"
        );

        assertEquals(
                "item : ${workouts}",
                workoutCard.attr("th:each")
        );
    }

    @Test
    void displaysPlannedDay() {
        Element workoutDay =
                document.selectFirst(".workout-day");

        assertNotNull(
                workoutDay,
                "Expected planned workout day"
        );

        String expression =
                workoutDay.attr("th:text");

        assertTrue(
                expression.contains(
                        "item.workout.dayOfWeek"
                ),
                "Expected dayOfWeek to drive planned day"
        );
    }

    @Test
    void displaysExerciseCount() {
        Element exerciseCount =
                document.selectFirst(".exercise-count");

        assertNotNull(
                exerciseCount,
                "Expected exercise count"
        );

        assertTrue(
                exerciseCount
                        .attr("th:text")
                        .contains(
                                "#lists.size(item.workout.exercises)"
                        ),
                "Expected exercise collection size"
        );
    }

    @Test
    void displaysTotalAttemptCount() {
        Element sessionCount =
                document.selectFirst(
                        ".workout-session-count"
                );

        assertNotNull(
                sessionCount,
                "Expected workout attempt count"
        );

        assertEquals(
                "${item.sessionCount > 0}",
                normalise(
                        sessionCount.attr("th:if")
                )
        );

        assertTrue(
                sessionCount
                        .attr("th:text")
                        .contains("item.sessionCount"),
                "Expected sessionCount to drive attempt text"
        );
    }

    @Test
    void completionIndicatorDependsOnCompletedState() {
        Element complete =
                document.selectFirst(
                        ".workout-complete"
                );

        assertNotNull(
                complete,
                "Expected workout completion indicator"
        );

        assertEquals(
                "${item.done}",
                complete.attr("th:if")
        );
    }

    @Test
    void completedSessionCountIsDisplayedOnlyWhenPositive() {
        Element completionCount =
                document.selectFirst(
                        ".workout-completion-count"
                );

        assertNotNull(
                completionCount,
                "Expected workout completion count"
        );

        assertEquals(
                "${item.completedSessionCount > 0}",
                normalise(
                        completionCount.attr("th:if")
                )
        );

        assertTrue(
                completionCount
                        .attr("th:text")
                        .contains(
                                "item.completedSessionCount"
                        ),
                "Expected completedSessionCount to drive text"
        );
    }

    @Test
    void completedStylingIsDrivenByWorkoutProgressDone() {
        Element workoutCard =
                document.selectFirst(".workout-card");

        assertNotNull(workoutCard);

        String classExpression =
                normalise(
                        workoutCard.attr(
                                "th:classappend"
                        )
                );

        assertTrue(
                classExpression.contains("${item.done}"),
                "Expected item.done to control completed styling"
        );

        assertTrue(
                classExpression.contains(
                        "workout-card-complete"
                ),
                "Expected completed workout CSS class"
        );
    }

    @Test
    void incompleteAttemptsDoNotDirectlyControlCompletedStyling() {
        Element workoutCard =
                document.selectFirst(".workout-card");

        assertNotNull(workoutCard);

        String classExpression =
                workoutCard.attr("th:classappend");

        assertTrue(
                classExpression.contains("item.done"),
                "Expected completion styling to use item.done"
        );

        assertFalse(
                classExpression.contains(
                        "item.sessionCount"
                ),
                """
                Attempt count should not directly control
                completed styling
                """
        );
    }

    @Test
    void workoutRemainsSelectableWhenComplete() {
        Element workoutCard =
                document.selectFirst(".workout-card");

        assertNotNull(workoutCard);

        Element previewLink =
                workoutCard.selectFirst("a");

        assertNotNull(
                previewLink,
                """
                Expected workout preview link to remain
                available independently of completion state
                """
        );

        /*
         * There must not be a th:if tied to !item.done
         * on the preview link.
         */
        assertFalse(
                previewLink
                        .attr("th:if")
                        .contains("item.done"),
                """
                Completed workout should remain selectable
                """
        );
    }

    @Test
    void workoutLinkUsesProgrammeWeekPreviewRoute() {
        Element workoutCard =
                document.selectFirst(".workout-card");

        assertNotNull(workoutCard);

        Element previewLink =
                workoutCard.selectFirst("a");

        assertNotNull(
                previewLink,
                "Expected workout preview link"
        );

        String href =
                normalise(
                        previewLink.attr("th:href")
                );

        assertTrue(
                href.contains(
                        "/programmes/{programmeId}"
                ),
                "Expected programme id in preview route"
        );

        assertTrue(
                href.contains(
                        "/weeks/{weekNumber}"
                ),
                "Expected week number in preview route"
        );

        assertTrue(
                href.contains(
                        "/workouts/{workoutId}"
                ),
                "Expected workout id in preview route"
        );

        assertTrue(
                href.contains(
                        "programmeId=${programme.id}"
                ),
                "Expected programme id binding"
        );

        assertTrue(
                href.contains(
                        "weekNumber=${week.number}"
                ),
                "Expected week number binding"
        );

        assertTrue(
                href.contains(
                        "workoutId=${item.workout.id}"
                ),
                "Expected workout id binding"
        );
    }

    private static String normalise(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}