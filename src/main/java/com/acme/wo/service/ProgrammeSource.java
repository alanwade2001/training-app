package com.acme.wo.service;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;

import java.util.List;

public interface ProgrammeSource {
    List<Programme> getProgrammes();
}
