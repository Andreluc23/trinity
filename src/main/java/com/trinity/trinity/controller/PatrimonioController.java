package com.trinity.trinity.controller;

import com.trinity.trinity.model.Patrimonio;
import com.trinity.trinity.service.PatrimonioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patrimonios")
public class PatrimonioController {

    private static final String REDIRECT_LISTA = "redirect:/patrimonios";

    private final PatrimonioService patrimonioService;

    public PatrimonioController(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "termo", required = false) String termo, Model model) {
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
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("patrimonio", patrimonioService.buscarPorId(id));
        return "forms/patrimonio-form";
    }

    @PostMapping
    public String salvar(Patrimonio patrimonio, RedirectAttributes redirectAttributes) {
        patrimonioService.salvar(patrimonio);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio salvo com sucesso.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        patrimonioService.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio desativado.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        patrimonioService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Patrimônio ativado.");
        return REDIRECT_LISTA;
    }
}