package com.trinity.trinity.controller;

import com.trinity.trinity.model.Patrimonio;
import com.trinity.trinity.service.PatrimonioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patrimonios")
public class PatrimonioController {

    private final PatrimonioService patrimonioService;

    public PatrimonioController(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String termo,
            Model model) {

        model.addAttribute("patrimonios", patrimonioService.buscar(termo));
        model.addAttribute("termo", termo);

        return "patrimonios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {

        model.addAttribute("patrimonio", new Patrimonio());

        return "forms/patrimonio-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        model.addAttribute(
                "patrimonio",
                patrimonioService.buscarPorId(id)
        );

        return "forms/patrimonio-form";
    }

    @PostMapping
    public String salvar(Patrimonio patrimonio) {

        patrimonioService.salvar(patrimonio);

        return "redirect:/patrimonios";
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable Long id) {

        patrimonioService.desativar(id);

        return "redirect:/patrimonios";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id) {

        patrimonioService.ativar(id);

        return "redirect:/patrimonios";
    }
}