package com.acme.wo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WorkoutAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkoutAppApplication.class, args);
    }

}
