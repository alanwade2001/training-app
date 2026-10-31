package com.acme.wo.internal.session;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SessionTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     SessionTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/session.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/session.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void readyViewRendersCorrectly() {
        Element screen =
                document.selectFirst(".ready-screen");

        assertNotNull(screen);

        assertStateCondition(
                screen,
                "READY"
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.name"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "execution.currentSet"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.sets"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.reps"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.recoveryInSecs"
                )
        );

        Element action =
                findActionForState("READY");

        assertNotNull(action);

        Element form =
                action.selectFirst("form");

        assertNotNull(form);

        assertTrue(
                normalise(form.attr("th:action"))
                        .contains(
                                "/sessions/{sessionId}/sets/start"
                        )
        );

        assertEquals(
                "post",
                form.attr("method")
        );
    }

    @Test
    void activeViewRendersCorrectly() {
        Element screen =
                document.selectFirst(".active-screen");

        assertNotNull(screen);

        assertStateCondition(
                screen,
                "ACTIVE"
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.name"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "execution.currentSet"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.sets"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.reps"
                )
        );

        Element action =
                findActionForState("ACTIVE");

        assertNotNull(action);

        Element form =
                action.selectFirst("form");

        assertNotNull(form);

        assertTrue(
                normalise(form.attr("th:action"))
                        .contains(
                                "/sessions/{sessionId}/sets/complete"
                        )
        );
    }

    @Test
    void restingViewRendersCorrectly() {
        Element screen =
                document.selectFirst(".rest-screen");

        assertNotNull(screen);

        assertStateCondition(
                screen,
                "RESTING"
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.name"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "execution.currentSet + 1"
                )
        );

        Element action =
                findActionForState("RESTING");

        assertNotNull(action);

        Element form =
                action.selectFirst("form");

        assertNotNull(form);

        assertTrue(
                normalise(form.attr("th:action"))
                        .contains(
                                "/sessions/{sessionId}/rest/complete"
                        )
        );
    }

    @Test
    void exerciseCompleteViewRendersCorrectly() {
        Element screen =
                document.selectFirst(
                        ".exercise-complete-screen"
                );

        assertNotNull(screen);

        assertStateCondition(
                screen,
                "EXERCISE_COMPLETE"
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.name"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.sets"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exercise.reps"
                )
        );

        Element action =
                findActionForState(
                        "EXERCISE_COMPLETE"
                );

        assertNotNull(action);

        Element form =
                action.selectFirst("form");

        assertNotNull(form);

        assertTrue(
                normalise(form.attr("th:action"))
                        .contains(
                                "/sessions/{sessionId}/exercises/next"
                        )
        );
    }

    @Test
    void sessionCompleteViewRendersCorrectly() {
        Element screen =
                document.selectFirst(
                        ".workout-complete-screen"
                );

        assertNotNull(screen);

        assertStateCondition(
                screen,
                "SESSION_COMPLETE"
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "session.workoutName"
                )
        );

        assertTrue(
                containsExpression(
                        screen,
                        "th:text",
                        "exerciseCount"
                )
        );
    }

    @Test
    void exitActionUsesContextAwareControllerRoute() {
        Element exit =
                document.selectFirst(".exit-button");

        assertNotNull(
                exit,
                "Expected Exit action"
        );

        String href =
                normalise(
                        exit.attr("th:href")
                );

        assertTrue(
                href.contains(
                        "/sessions/{sessionId}/exit"
                ),
                "Expected session Exit controller route"
        );

        assertTrue(
                href.contains(
                        "sessionId=${sessionId}"
                ),
                "Expected current session ID"
        );
    }

    @Test
    void exitIsHiddenAfterSessionComplete() {
        Element exit =
                document.selectFirst(".exit-button");

        assertNotNull(exit);

        String condition =
                normalise(
                        exit.attr("th:if")
                );

        assertTrue(
                condition.contains(
                        "execution.state.name()"
                )
        );

        assertTrue(
                condition.contains(
                        "!= 'SESSION_COMPLETE'"
                ),
                """
                Expected Exit to be hidden after
                SESSION_COMPLETE
                """
        );
    }

    @Test
    void finalDoneActionUsesControllerRoute() {
        Element action =
                findActionForState(
                        "SESSION_COMPLETE"
                );

        assertNotNull(
                action,
                "Expected completed-session action"
        );

        Element form =
                action.selectFirst("form");

        assertNotNull(
                form,
                "Expected completed-session form"
        );

        assertEquals(
                "post",
                form.attr("method")
        );

        String actionExpression =
                normalise(
                        form.attr("th:action")
                );

        assertTrue(
                actionExpression.contains(
                        "/sessions/{sessionId}/complete"
                ),
                "Expected session complete controller route"
        );

        assertTrue(
                actionExpression.contains(
                        "sessionId=${sessionId}"
                ),
                "Expected current session ID"
        );

        Element button =
                form.selectFirst("button");

        assertNotNull(button);

        assertEquals(
                "DONE",
                button.text().trim()
        );
    }

    private static Element findActionForState(
            String state
    ) {
        return document
                .select(".session-actions")
                .stream()
                .filter(element ->
                        normalise(
                                element.attr("th:if")
                        ).contains(
                                "== '" + state + "'"
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private static void assertStateCondition(
            Element element,
            String state
    ) {
        String condition =
                normalise(
                        element.attr("th:if")
                );

        assertTrue(
                condition.contains(
                        "execution.state.name()"
                ),
                "Expected execution state condition"
        );

        assertTrue(
                condition.contains(
                        "== '" + state + "'"
                ),
                "Expected state " + state
        );
    }

    private static boolean containsExpression(
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

    private static String normalise(
            String value
    ) {
        return value
                .replaceAll("\\s+", " ")
                .replace("\\_", "_")
                .trim();
    }
}