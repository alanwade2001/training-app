package com.acme.wo.internal.programme;

import com.acme.wo.model.Programme;
import com.acme.wo.model.Workout;
import com.acme.wo.service.ProgrammeService;
import com.acme.wo.service.SessionService;
import com.acme.wo.service.UploadValidationException;
import com.acme.wo.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/programmes")
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final SessionService sessionService;
    private final WorkoutService workoutService;

    public ProgrammeController(
            ProgrammeService programmeService, SessionService sessionService, WorkoutService workoutService) {

        this.programmeService = programmeService;
        this.sessionService = sessionService;
        this.workoutService = workoutService;
    }

    @PostMapping("/upload")
    public String uploadProgramme(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            var programme = programmeService.persist(file);

            return "redirect:/programmes/" + programme.id();
        }catch( UploadValidationException ex ) {
            redirectAttributes.addFlashAttribute(
                    "uploadErrors",
                    ex.errors()
            );

            return "redirect:/programmes";

        }
    }

    @GetMapping
    public String programmes(Model model) {
        var programmes = programmeService.findAll();

        model.addAttribute("programmes", programmes);

        return "programmes";
    }

    @GetMapping("/{programmeId}")
    public String programme(
            @PathVariable Long programmeId,
            Model model) {

        var programme =
                programmeService.findById(programmeId);

        var weeks = programme.weeks().stream()
                .map(week -> {

                    long completedWorkouts =
                            week.workouts().stream()
                                    .filter(workout ->
                                            sessionService.countCompletedByWorkoutId(
                                                    workout.id()
                                            ) > 0
                                    )
                                    .count();

                    return new WeekProgress(
                            week,
                            completedWorkouts
                    );
                })
                .toList();

        model.addAttribute("programme", programme);
        model.addAttribute("weeks", weeks);

        return "programme";
    }

    @GetMapping("/{programmeId}/weeks/{weekNumber}")
    public String showWeek(
            @PathVariable Long programmeId,
            @PathVariable int weekNumber,
            Model model) {

        var programme = programmeService.findById(programmeId);

        var week = programme.weeks().stream()
                .filter(w -> w.number() == weekNumber)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Week not found: " + weekNumber
                ));

        var workouts = week.workouts().stream()
                .map(workout -> new WorkoutProgress(
                        workout,
                        sessionService.countByWorkoutId(workout.id()),
                        sessionService.countCompletedByWorkoutId(workout.id())
                ))
                .toList();



        long completedWorkouts = workouts.stream()
                .filter(WorkoutProgress::done)
                .count();

        model.addAttribute("programme", programme);
        model.addAttribute("week", week);
        model.addAttribute("workouts", workouts);
        model.addAttribute("completedWorkouts", completedWorkouts);

        return "week";
    }

    @PostMapping("/{programmeId}/delete")
    public String deleteProgramme(@PathVariable Long programmeId) {

        programmeService.deleteProgramme(programmeId);

        return "redirect:/programmes";
    }

    @GetMapping("/{programmeId}/weeks/{weekNumber}/workouts/{workoutId}")
    public String workout(
            @PathVariable Long programmeId,
            @PathVariable Integer weekNumber,
            @PathVariable Long workoutId,
            Model model) {

        Workout workout = workoutService.findById(workoutId);

        model.addAttribute("programmeId", programmeId);
        model.addAttribute("weekNumber", weekNumber);
        model.addAttribute("workout", workout);

        var returnUrl = "/programmes/" + programmeId + "/weeks/"+ weekNumber;
        model.addAttribute("returnUrl", returnUrl);

        return "workout";
    }
}


