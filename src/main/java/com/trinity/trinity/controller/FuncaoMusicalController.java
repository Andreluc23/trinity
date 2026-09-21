package com.trinity.trinity.controller;

import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.service.FuncaoMusicalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/funcoes-musicais")
public class FuncaoMusicalController {

    private final FuncaoMusicalService funcaoMusicalService;

    public FuncaoMusicalController(FuncaoMusicalService funcaoMusicalService) {
        this.funcaoMusicalService = funcaoMusicalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());
        return "funcoes-musicais";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("funcaoMusical", new FuncaoMusical());
        return "funcao-musical-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute(
                "funcaoMusical",
                funcaoMusicalService.buscarPorId(id)
        );

        return "funcao-musical-form";
    }

    @PostMapping
    public String salvar(FuncaoMusical funcaoMusical) {
        funcaoMusicalService.salvar(funcaoMusical);
        return "redirect:/funcoes-musicais";
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable Long id) {
        funcaoMusicalService.desativar(id);
        return "redirect:/funcoes-musicais";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id) {
        funcaoMusicalService.ativar(id);
        return "redirect:/funcoes-musicais";
    }
}