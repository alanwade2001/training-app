package com.acme.wo.regression;

import com.acme.wo.model.Programme;
import com.acme.wo.service.ProgrammeService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadValidationJourneySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private JourneyState state;

    @Given("Programme YAML has exercise recovery of {int} seconds")
    public void programmeYamlHasExerciseRecovery(
            int recovery
    ) {
        String programmeName =
                "Cucumber Validation Programme";

        state.setUploadedProgrammeName(
                programmeName
        );

        state.setProgrammeCountBeforeUpload(
                programmeService.findAll().size()
        );

        state.setUploadYaml(
                yaml(programmeName, recovery)
        );
    }

    @When("I upload the Programme")
    public void uploadProgramme()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "programme.yaml",
                        "application/x-yaml",
                        state.getUploadYaml()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        MvcResult result =
                mockMvc.perform(
                                multipart(
                                        "/programmes/upload"
                                )
                                        .file(file)
                        )
                        .andReturn();

        state.setLastResult(result);
    }

    @Then("the Programme upload is rejected")
    public void programmeUploadRejected() {

        MvcResult result =
                state.getLastResult();

        assertNotNull(result);

        assertTrue(
                result.getResponse()
                        .getStatus()
                        >= 300
                        && result.getResponse()
                        .getStatus()
                        < 400
        );

        assertEquals(
                "/programmes",
                result.getResponse()
                        .getRedirectedUrl()
        );
    }

    @And("validation feedback is returned")
    public void validationFeedbackReturned() {

        var flashMap =
                state.getLastResult()
                        .getFlashMap();

        assertTrue(
                flashMap.containsKey("uploadErrors")
        );

        assertNotNull(
                flashMap.get("uploadErrors")
        );
    }

    @And("the invalid Programme is not persisted")
    public void invalidProgrammeNotPersisted() {

        List<Programme> programmes =
                programmeService.findAll();

        assertEquals(
                state.getProgrammeCountBeforeUpload(),
                programmes.size()
        );

        assertTrue(
                programmes.stream()
                        .noneMatch(programme ->
                                state.getUploadedProgrammeName()
                                        .equals(
                                                programme.name()
                                        )
                        )
        );
    }

    @When("I change the exercise recovery to {int} seconds")
    public void changeExerciseRecovery(
            int recovery
    ) {
        state.setUploadYaml(
                yaml(
                        state.getUploadedProgrammeName(),
                        recovery
                )
        );
    }

    @Then("the Programme upload succeeds")
    public void programmeUploadSucceeds() {

        MvcResult result =
                state.getLastResult();

        assertNotNull(result);

        assertTrue(
                result.getResponse()
                        .getStatus()
                        >= 300
                        && result.getResponse()
                        .getStatus()
                        < 400
        );

        String redirect =
                result.getResponse()
                        .getRedirectedUrl();

        assertNotNull(redirect);

        assertTrue(
                redirect.startsWith(
                        "/programmes/"
                ),
                "Unexpected redirect: " + redirect
        );

        state.setProgrammeId(
                Long.valueOf(
                        redirect.substring(
                                "/programmes/".length()
                        )
                )
        );
    }

    @And("the Programme is persisted")
    public void programmeIsPersisted() {

        Programme programme =
                programmeService.findById(
                        state.getProgrammeId()
                );

        assertNotNull(programme);

        assertEquals(
                state.getUploadedProgrammeName(),
                programme.name()
        );
    }

    @And("its complete hierarchy is persisted")
    public void completeHierarchyIsPersisted() {

        Programme programme =
                programmeService.findById(
                        state.getProgrammeId()
                );

        assertNotNull(programme);
        assertEquals(1, programme.weeks().size());

        var week =
                programme.weeks().getFirst();

        assertEquals(1, week.number());
        assertEquals(1, week.workouts().size());

        var workout =
                week.workouts().getFirst();

        assertEquals(
                "Validation Workout",
                workout.name()
        );

        assertEquals(
                1,
                workout.exercises().size()
        );

        var exercise =
                workout.exercises().getFirst();

        assertEquals(
                "400m",
                exercise.name()
        );

        assertEquals(
                15,
                exercise.recoveryInSecs()
        );
    }

    private String yaml(
            String programmeName,
            int recovery
    ) {
        return """
                name: %s
                weeks:
                  - number: 1
                    workouts:
                      - name: Validation Workout
                        plannedDay: TUESDAY
                        exercises:
                          - name: 400m
                            sets: 1
                            reps: 1
                            quantity: 400
                            units: METERS
                            recoveryInSecs: %d
                """.formatted(
                programmeName,
                recovery
        );
    }
}