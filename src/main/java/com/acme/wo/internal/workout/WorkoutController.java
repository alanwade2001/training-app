package com.acme.wo.internal.workout;

import com.acme.wo.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(
            WorkoutService workoutService) {

        this.workoutService = workoutService;
    }

    @PostMapping("/upload")
    public String uploadProgramme(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        var workout = workoutService.persist(file);

        return "redirect:/workouts";
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
