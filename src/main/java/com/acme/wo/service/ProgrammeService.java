package com.acme.wo.service;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProgrammeService {

    private final List<ProgrammeSource> sources;

    public ProgrammeService(List<ProgrammeSource> sources) {
        this.sources = sources;
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
}
