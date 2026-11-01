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
import java.util.Optional;

@Service
public class ProgrammeService {

    private final YamlUploadService uploadService;
    private final ProgrammePersistor programmePersistor;

    public ProgrammeService(YamlUploadService uploadService, ProgrammePersistor programmePersistor) {
        this.uploadService = uploadService;
        this.programmePersistor = programmePersistor;
    }


    public List<Programme> findAll() {
        return programmePersistor.findAll();
    }

    public Optional<Programme> findById(Long programmeId) {
        return programmePersistor.findById(programmeId);
    }

    public Programme persist(InputStreamSource source) {
        var programme = uploadService.importProgramme(source);
        return programmePersistor.persist(programme);
    }

    public void deleteProgramme(Long programmeId) {
        programmePersistor.deleteById(programmeId);
    }
}
