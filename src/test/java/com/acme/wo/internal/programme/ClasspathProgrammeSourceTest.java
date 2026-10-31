package com.acme.wo.internal.programme;

import com.acme.wo.internal.workout.ClasspathWorkoutSource;
import com.acme.wo.service.YamlUploadService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({YamlUploadService.class})
@Disabled("Classpath programme source will be replaced by database seeding in v4")
class ClasspathProgrammeSourceTest {
    @Autowired
    YamlUploadService uploadService;

    @Test
    void getProgrammes() throws IOException {
        var source = new ClasspathProgrammeSource(uploadService);
        var programmes = source.getProgrammes();
        Assertions.assertThat(programmes).isNotEmpty();
    }
}