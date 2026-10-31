package com.acme.wo.internal.home;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeTemplateTest {

    @Test
    void containsMainNavigationLinks() throws IOException {

        try (InputStream input =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/home.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/home.html was not found"
            );

            Document document =
                    Jsoup.parse(
                            input,
                            "UTF-8",
                            ""
                    );

            assertLink(document, "/programmes");
            assertLink(document, "/workouts");
            assertLink(document, "/history");
        }
    }

    private void assertLink(
            Document document,
            String expectedPath
    ) {
        boolean found = document
                .select("a")
                .stream()
                .anyMatch(link ->
                        expectedPath.equals(
                                link.attr("href")
                        )
                                ||
                                ("@{" + expectedPath + "}")
                                        .equals(
                                                link.attr("th:href")
                                        )
                );

        assertTrue(
                found,
                "Expected link to " + expectedPath
        );
    }
}