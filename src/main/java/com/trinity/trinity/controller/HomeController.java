package com.trinity.trinity.controller;

import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MembroService membroService;

    public HomeController(MembroService membroService) {
        this.membroService = membroService;
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

        return "dashboard";
    }
}