package com.acme.wo.internal.workout;

import com.acme.wo.model.Workout;
import com.acme.wo.service.UploadValidationException;
import com.acme.wo.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            var workout = workoutService.persist(file);

            return "redirect:/workouts";
        }catch( UploadValidationException ex ) {
            redirectAttributes.addFlashAttribute(
                    "uploadErrors",
                    ex.errors()
            );

            return "redirect:/workouts";

        }
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
