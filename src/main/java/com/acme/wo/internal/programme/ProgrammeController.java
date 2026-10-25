package com.acme.wo.internal.programme;

import com.acme.wo.model.Programme;
import com.acme.wo.service.ProgrammeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/programmes")
public class ProgrammeController {

    private final ProgrammeService programmeService;

    public ProgrammeController(
            ProgrammeService programmeService) {

        this.programmeService = programmeService;
    }

    @PostMapping("/upload")
    public String uploadProgramme(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        var programme = programmeService.persist(file);

        return "redirect:/programmes/" + programme.id();
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

        model.addAttribute("programme", programme);

        return "programme";
    }

    @GetMapping("/{programmeId}/weeks/{weekNumber}")
    public String week(
            @PathVariable Long programmeId,
            @PathVariable int weekNumber,
            Model model) {

        var programme = programmeService.findById(programmeId);

        var week = programme.weeks().stream()
                .filter(w -> w.number() == weekNumber)
                .findFirst()
                .orElseThrow();

        model.addAttribute("programme", programme);
        model.addAttribute("week", week);

        return "week";
    }
}


