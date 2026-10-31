package com.acme.wo.regression;

import io.cucumber.spring.ScenarioScope;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

@Data
@Component
@ScenarioScope
public class JourneyState {

    private Long programmeId;

    private int weekNumber;

    private Long workoutId;

    private Long sessionId;

    private long initialCompletedWorkouts;

    private MvcResult lastResult;

    private String expectedWorkoutName;
    private String expectedExerciseName;

    private String uploadYaml;
    private String uploadedProgrammeName;

    private int programmeCountBeforeUpload;

    private Long weekId;
    private Long workoutExerciseId;


}