package com.acme.wo.internal.programme;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;

import java.util.List;

public interface ProgrammePersistor {
    List<Programme> persist(List<Programme> programmes);

    Programme persist(Programme p);
}
