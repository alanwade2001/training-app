package com.acme.wo.internal.programme;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ProgrammesTemplateTest {

    private static Document document;

    @BeforeAll
    static void loadTemplate() throws IOException {
        try (InputStream input =
                     ProgrammesTemplateTest.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "templates/programmes.html"
                             )) {

            assertNotNull(
                    input,
                    "templates/programmes.html was not found"
            );

            document = Jsoup.parse(
                    input,
                    "UTF-8",
                    ""
            );
        }
    }

    @Test
    void rendersAllProgrammes() {
        Element programmeCard =
                document.selectFirst(
                        "a.programme-card"
                );

        assertNotNull(
                programmeCard,
                "Expected a programme card"
        );

        assertEquals(
                "programme : ${programmes}",
                programmeCard.attr("th:each")
        );

        Element title =
                programmeCard.selectFirst(
                        ".programme-card-title"
                );

        assertNotNull(
                title,
                "Expected programme title"
        );

        assertEquals(
                "${programme.name}",
                title.attr("th:text")
        );
    }

    @Test
    void programmeCardLinksToSelectedProgramme() {
        Element programmeCard =
                document.selectFirst(
                        "a.programme-card"
                );

        assertNotNull(programmeCard);

        String href =
                programmeCard.attr("th:href");

        assertTrue(
                href.contains(
                        "/programmes/{programmeId}"
                ),
                "Expected programme URL"
        );

        assertTrue(
                href.contains(
                        "programmeId=${programme.id}"
                ),
                "Expected programme id in URL"
        );
    }

    @Test
    void uploadPanelIsCollapsedByDefault() {
        Element uploadPanel =
                document.selectFirst(
                        "details.upload-panel"
                );

        assertNotNull(
                uploadPanel,
                "Expected upload panel"
        );

        assertFalse(
                uploadPanel.hasAttr("open"),
                "Upload panel should be collapsed by default"
        );
    }

    @Test
    void uploadFormPostsToProgrammeUploadEndpoint() {
        Element uploadForm =
                document.selectFirst(
                        "form.upload-form"
                );

        assertNotNull(
                uploadForm,
                "Expected upload form"
        );

        assertEquals(
                "@{/programmes/upload}",
                uploadForm.attr("th:action")
        );

        assertEquals(
                "post",
                uploadForm.attr("method")
        );

        assertEquals(
                "multipart/form-data",
                uploadForm.attr("enctype")
        );

        Element fileInput =
                uploadForm.selectFirst(
                        "input[type=file][name=file]"
                );

        assertNotNull(
                fileInput,
                "Expected programme YAML file input"
        );
    }

    @Test
    void validationErrorsCanBeDisplayed() {
        Element errorContainer =
                document.selectFirst(".upload-errors");

        assertNotNull(
                errorContainer,
                "Expected upload-errors container"
        );

        assertEquals(
                "${uploadErrors != null}",
                errorContainer.attr("th:if")
        );

        Element error =
                errorContainer
                        .getAllElements()
                        .stream()
                        .filter(element ->
                                element.hasAttr("th:each")
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(
                error,
                "Expected validation errors to be iterated"
        );

        assertEquals(
                "error : ${uploadErrors}",
                error.attr("th:each")
        );

        assertEquals(
                "${error}",
                error.attr("th:text")
        );
    }

    @Test
    void validationFailureCanReopenUploadPanel() {
        Element uploadPanel =
                document.selectFirst(
                        "details.upload-panel"
                );

        assertNotNull(uploadPanel);

        String conditionalOpen =
                uploadPanel.attr("th:attr");

        assertTrue(
                conditionalOpen.contains("uploadErrors"),
                """
                Expected upload panel to conditionally open
                when uploadErrors are present
                """
        );
    }
}