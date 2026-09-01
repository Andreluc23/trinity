package com.trinity.trinity.controller;

import com.trinity.trinity.model.Membro;
import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MembroController {

    private final MembroService membroService;

    public MembroController(MembroService membroService) {
        this.membroService = membroService;
    }

    @GetMapping("/membros")
    public String listarMembros(Model model) {

        model.addAttribute("membros", membroService.listarTodos());

        return "membros";
    }

    @GetMapping("/membros/novo")
    public String novoMembro(Model model) {

        model.addAttribute("membro", new Membro());

        return "membro-form";
    }
    @PostMapping("/membros")
    public String salvarMembro(Membro membro) {

        membroService.salvar(membro);

        return "redirect:/membros";
    }
}