package com.trinity.trinity.controller;

import com.trinity.trinity.model.Aviso;
import com.trinity.trinity.service.AvisoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AvisoController {

    private final AvisoService avisoService;

    public AvisoController(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @GetMapping("/avisos")
    public String listarAvisos(Model model) {

        model.addAttribute("avisos", avisoService.listarTodos());

        return "avisos";
    }

    @GetMapping("/avisos/novo")
    public String novoAviso(Model model) {

        model.addAttribute("aviso", new Aviso());

        return "forms/aviso-form";
    }

    @GetMapping("/avisos/editar/{id}")
    public String editarAviso(@PathVariable Long id, Model model) {

        model.addAttribute("aviso", avisoService.buscarPorId(id));

        return "forms/aviso-form";
    }

    @PostMapping("/avisos")
    public String salvarAviso(Aviso aviso) {

        avisoService.salvar(aviso);

        return "redirect:/avisos";
    }

    @PostMapping("/avisos/desativar/{id}")
    public String desativarAviso(@PathVariable Long id) {

        avisoService.desativar(id);

        return "redirect:/avisos";
    }

    @PostMapping("/avisos/ativar/{id}")
    public String ativarAviso(@PathVariable Long id) {

        avisoService.ativar(id);

        return "redirect:/avisos";
    }
}