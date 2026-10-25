package com.acme.wo.internal.workout;

import com.acme.wo.service.YamlUploadService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({YamlUploadService.class})
class ClasspathWorkoutSourceTest {

    @Autowired
    private YamlUploadService uploadService;

    @Test
    void loadWorkoutDefinitions() throws IOException {
        var source = new ClasspathWorkoutSource(uploadService);
        var workouts = source.getWorkouts();
        Assertions.assertThat(workouts).isNotEmpty();

    }

}