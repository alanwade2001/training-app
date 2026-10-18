package com.acme.wo.service;

import com.acme.wo.internal.workout.HardcodedWorkoutService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class HardcodedConfig {
    @Primary
    @Bean
    HardcodedWorkoutService workoutService() {
        return new HardcodedWorkoutService();
    }
}
