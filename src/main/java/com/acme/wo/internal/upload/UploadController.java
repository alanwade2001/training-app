package com.acme.wo.internal.upload;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.service.YamlUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final YamlUploadService uploadService;
    private final WorkoutPersistor workoutPersistor;
    private final ProgrammePersistor programmePersistor;

    public UploadController(YamlUploadService uploadService, WorkoutPersistor workoutPersistor, ProgrammePersistor programmePersistor) {
        this.uploadService = uploadService;
        this.workoutPersistor = workoutPersistor;
        this.programmePersistor = programmePersistor;
    }


    @PostMapping(
            value = "/workout",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadWorkout(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        // YAML → Workout → persist
        var workoutDefinition = uploadService.importWorkout(file);
        var persistedWorkouts = workoutPersistor.persist(workoutDefinition.workouts());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(
            value = "/programme",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProgramme(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        // YAML → Programme → persist
        var programme = uploadService.importProgramme(file);
        var persistedProgramme = programmePersistor.persist(programme);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
