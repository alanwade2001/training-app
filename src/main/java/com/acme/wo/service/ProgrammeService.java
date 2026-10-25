package com.acme.wo.service;

import com.acme.wo.internal.programme.ProgrammePersistor;
import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProgrammeService {

    private final List<ProgrammeSource> sources;
    private final YamlUploadService uploadService;
    private final ProgrammePersistor programmePersistor;

    public ProgrammeService(List<ProgrammeSource> sources, YamlUploadService uploadService, ProgrammePersistor programmePersistor) {
        this.sources = sources;
        this.uploadService = uploadService;
        this.programmePersistor = programmePersistor;
    }


    public List<Programme> findAll() {
        var programmes = new ArrayList<Programme>();
        sources.forEach(s->programmes.addAll(s.getProgrammes()));
        programmes.sort(Comparator.comparingLong(Programme::id));
        return programmes;
    }

    public Programme findById(Long programmeId) {
        var programmes = findAll();
        return programmes.stream().filter(p->programmeId.equals(p.id())).findFirst().orElse(null);
    }

    public Programme persist(InputStreamSource source) {
        var programme = uploadService.importProgramme(source);
        return programmePersistor.persist(programme);
    }
}
