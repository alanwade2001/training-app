package com.acme.wo.internal.history;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class HistoryTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     HistoryTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/history.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/history.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void rendersSessionsFromControllerModel() {
        Element historyCard =
                document.selectFirst(".history-card");

        assertNotNull(
                historyCard,
                "Expected history card"
        );

        assertEquals(
                "s : ${sessions}",
                historyCard.attr("th:each")
        );
    }

    @Test
    void doesNotUseSessionAsIterationVariable() {
        Element historyCard =
                document.selectFirst(".history-card");

        assertNotNull(historyCard);

        String each =
                historyCard.attr("th:each");

        assertTrue(
                each.startsWith("s :"),
                "Expected safe iteration variable 's'"
        );

        assertFalse(
                each.startsWith("session :"),
                "Should not use 'session' as iteration variable"
        );
    }

    @Test
    void rendersWorkoutName() {
        Element workoutName =
                document.selectFirst(
                        ".history-card-title"
                );

        assertNotNull(
                workoutName,
                "Expected workout name"
        );

        assertEquals(
                "${s.workoutName}",
                workoutName.attr("th:text")
        );
    }

    @Test
    void rendersStartedAt() {
        Element date =
                document.selectFirst(
                        ".history-card-date time"
                );

        assertNotNull(
                date,
                "Expected session date"
        );

        assertEquals(
                "${s.startedAt}",
                date.attr("th:datetime")
        );

        assertTrue(
                normalise(
                        date.attr("th:text")
                ).contains("s.startedAt"),
                "Expected startedAt to drive session date"
        );

        assertTrue(
                containsAttributeValue(
                        document,
                        "th:text",
                        "s.startedAt"
                ),
                "Expected startedAt to be displayed"
        );
    }

    @Test
    void rendersUpdatedAt() {
        assertTrue(
                containsAttributeValue(
                        document,
                        "th:text",
                        "s.updatedAt"
                ),
                "Expected updatedAt to be displayed"
        );
    }

    @Test
    void linksToSessionDetail() {
        Element link =
                document.selectFirst(
                        ".history-card-action"
                );

        assertNotNull(
                link,
                "Expected session detail link"
        );

        String href =
                normalise(
                        link.attr("th:href")
                );

        assertTrue(
                href.contains(
                        "/history/{sessionId}"
                ),
                "Expected history detail endpoint"
        );

        assertTrue(
                href.contains(
                        "sessionId=${s.id}"
                ),
                "Expected session ID binding"
        );
    }

    private static boolean containsAttributeValue(
            Element root,
            String attribute,
            String value
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
                .anyMatch(expression ->
                        expression.contains(value)
                );
    }

    private static String normalise(String value) {
        return value
                .replaceAll("\\s+", " ")
                .trim();
    }
}