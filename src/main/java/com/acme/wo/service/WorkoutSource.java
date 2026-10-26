package com.acme.wo.service;


import com.acme.wo.model.Workout;

import java.io.IOException;
import java.util.List;

public interface WorkoutSource {
    List<Workout> getWorkouts();
}
