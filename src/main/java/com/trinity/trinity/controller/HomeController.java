package com.trinity.trinity.controller;

import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.trinity.trinity.service.AvisoService;

@Controller
public class HomeController {

    private final MembroService membroService;
    private final AvisoService avisoService;

    public HomeController(MembroService membroService, AvisoService avisoService) {
        this.membroService = membroService;
        this.avisoService = avisoService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalMembros", membroService.contarTodos());
        model.addAttribute("membrosAtivos", membroService.contarAtivos());
        model.addAttribute("membrosInativos", membroService.contarInativos());
        model.addAttribute("avisosRecentes", avisoService.listarRecentes());

        return "dashboard";
    }
}