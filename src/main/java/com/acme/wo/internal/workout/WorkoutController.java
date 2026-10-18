package com.acme.wo.internal.workout;

import com.acme.wo.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(
            WorkoutService workoutService) {

        this.workoutService = workoutService;
    }

    @GetMapping
    public String workouts(Model model) {

        model.addAttribute(
                "workouts",
                workoutService.findAll()
        );

        return "workouts";
    }
}
