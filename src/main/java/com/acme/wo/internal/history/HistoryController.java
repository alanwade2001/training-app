package com.acme.wo.internal.history;

import com.acme.wo.model.Session;
import com.acme.wo.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private final SessionService sessionService;

    public HistoryController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public String history(Model model) {

        model.addAttribute(
                "sessions",
                sessionService.history()
        );

        return "history";
    }

    @GetMapping("/{sessionId}")
    public String sessionDetail(
            @PathVariable Long sessionId,
            Model model) {

        Session session = sessionService.findById(sessionId);

        model.addAttribute("trainingsession", session);

        return "session-detail";
    }
}
