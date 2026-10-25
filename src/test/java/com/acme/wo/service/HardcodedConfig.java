package com.acme.wo.service;

import com.acme.wo.internal.workout.HardcodedWorkoutSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class HardcodedConfig {
    @Primary
    @Bean
    HardcodedWorkoutSource workoutSource() {
        return new HardcodedWorkoutSource();
    }
}
