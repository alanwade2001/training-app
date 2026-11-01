package com.acme.wo.internal.programme;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;

import java.util.List;
import java.util.Optional;

public interface ProgrammePersistor {
    List<Programme> persist(List<Programme> programmes);

    Programme persist(Programme p);

    void deleteById(Long programmeId);

    List<Programme> findAll();
    Optional<Programme> findById(Long programmeId);
}
