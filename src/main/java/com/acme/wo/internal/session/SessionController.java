package com.acme.wo.internal.session;

import com.acme.wo.model.*;
import com.acme.wo.service.SessionService;
import com.acme.wo.service.WorkoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final WorkoutService workoutService;


    public SessionController(
            SessionService sessionService, WorkoutService workoutService) {

        this.sessionService = sessionService;
        this.workoutService = workoutService;
    }


    @PostMapping
    public String createSession(
            @RequestParam Long workoutId) {

        Session session =
                sessionService.createSession(
                        workoutId
                );

        return "redirect:/sessions/"
                + session.id();
    }


    @GetMapping("/{sessionId}")
    public String session(
            @PathVariable Long sessionId,
            Model model) {

        Session session =
                sessionService.getSessionById(
                        sessionId
                );

        SessionExecution execution = session.execution();

        SessionExercise exercise = session.getCurrentExercise();


        model.addAttribute(
                "session",
                session
        );

        model.addAttribute(
                "execution",
                execution
        );

        model.addAttribute(
                "exercise",
                exercise
        );

        model.addAttribute(
                "exerciseNumber",
                execution.exerciseIndex() + 1
        );

        model.addAttribute(
                "exerciseCount",
                session.exercises().size()
        );


        return "session";
    }

    @PostMapping(
            "/{sessionId}/sets/start")
    public String startSet(
            @PathVariable Long sessionId) {

        sessionService.startSet(
                sessionId
        );

        return "redirect:/sessions/"
                + sessionId;
    }

    @PostMapping("/{sessionId}/sets/complete")
    public String completeSet(
            @PathVariable Long sessionId) {

        sessionService.completeSet(
                sessionId
        );

        return "redirect:/sessions/"
                + sessionId;
    }

    @PostMapping("/{sessionId}/rest/complete")
    public String completeRest(
            @PathVariable Long sessionId) {

        sessionService.completeRest(
                sessionId
        );

        return "redirect:/sessions/"
                + sessionId;
    }

    @PostMapping("/{sessionId}/exercises/next")
    public String nextExercise(
            @PathVariable Long sessionId) {

        sessionService.nextExercise(
                sessionId
        );

        return "redirect:/sessions/"
                + sessionId;
    }

    @PostMapping("/{sessionId}/complete")
    public String completeWorkout(
            @PathVariable Long sessionId) {

        Session session = sessionService.findById(sessionId);

        return workoutReturnUrl(session);
    }

    @GetMapping("/{sessionId}/exit")
    public String exit(@PathVariable Long sessionId) {

        Session session = sessionService.findById(sessionId);

        return workoutReturnUrl(session);
    }

    private String workoutReturnUrl(Session session) {

        String url = workoutService
                .findProgrammeLocation(session.workoutId())
                .map(location ->
                        "redirect:/programmes/"
                                + location.programmeId()
                                + "/weeks/"
                                + location.weekNumber()
                )
                .orElse("redirect:/workouts");

        log.atInfo().log("workout return url: {}", url);

        return url;
    }
}
