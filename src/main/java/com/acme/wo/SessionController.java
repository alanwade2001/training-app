package com.acme.wo;

import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;
import com.acme.wo.model.SessionExercise;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;


    public SessionController(
            SessionService sessionService) {

        this.sessionService = sessionService;
    }


    @PostMapping
    public String createSession(
            @RequestParam String workoutId) {

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
                sessionService.findById(
                        sessionId
                );

        SessionExecution execution =
                sessionService.execution(sessionId);

        SessionExercise exercise =
                sessionService.currentExercise(sessionId);


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
}
