package com.acme.wo.internal.programme;

import com.acme.wo.internal.utils.ClassPathResourceLoader;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;
import com.acme.wo.service.ProgrammeSource;
import com.acme.wo.service.WorkoutSource;
import com.acme.wo.service.YamlUploadService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClasspathProgrammeSource implements ProgrammeSource, WorkoutSource {
    @Override
    public List<Workout> getWorkouts() {
        var programmes = getProgrammes();
        return programmes.stream()
                .flatMap(programme -> programme.weeks().stream())
                .flatMap(week -> week.workouts().stream())
                .toList();
    }

    private final YamlUploadService uploadService;

    public ClasspathProgrammeSource(YamlUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Override
    public List<Programme> getProgrammes() {
        var loader = new ClassPathResourceLoader();

        var resources = loader.load("classpath:programmes/*.yml");

        var programmes = new ArrayList<Programme>();

        var nextid = 100L;

        for (Resource r : resources) {
            var programme = uploadService.importProgramme(r);
            var prog = new Programme(nextid++, programme.name(), programme.weeks());

            programmes.add(prog);
        }

            return programmes;
    }


}
