package com.acme.wo.internal.programme;

import com.acme.wo.model.Workout;
import com.acme.wo.service.ProgrammeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/programme")
public class ProgrammeController {

    private final ProgrammeService programmeService;

    public ProgrammeController(
            ProgrammeService programmeService) {

        this.programmeService = programmeService;
    }

    @GetMapping
    public String programme(Model model) {
        var programme = programmeService.findAll().getFirst();

        model.addAttribute("programme", programme);

        return "programme";
    }

    @GetMapping("/weeks/{weekNumber}")
    public String week(
            @PathVariable int weekNumber,
            Model model) {

        var programme = programmeService.findAll().getFirst();

        var week = programme.weeks().stream()
                .filter(w -> w.number() == weekNumber)
                .findFirst()
                .orElseThrow();

        model.addAttribute("programme", programme);
        model.addAttribute("week", week);

        return "week";
    }

//    @GetMapping("/weeks/{weekNumber}/workouts/{workoutIndex}")
//    public String selectWorkout(
//            @PathVariable int weekNumber,
//            @PathVariable int workoutIndex) {
//
//        var programme = programmeService.findAll().getFirst();
//
//        var week = programme.weeks().stream()
//                .filter(candidate -> candidate.number() == weekNumber)
//                .findFirst()
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Week not found: " + weekNumber
//                ));
//
//        if (workoutIndex < 0 || workoutIndex >= week.workouts().size()) {
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND,
//                    "Workout not found: " + workoutIndex
//            );
//        }
//
//        Workout workout = week.workouts().get(workoutIndex);
//
//        // Hand workout into your existing session mechanism here.
//
//        return "redirect:/workout";
//    }
}
