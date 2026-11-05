package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.service.ContribuicaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/financeiro/contribuicoes")
public class ContribuicaoAdminController {

    private static final String REDIRECT_LISTA = "redirect:/financeiro/contribuicoes";

    private final ContribuicaoService contribuicaoService;

    public ContribuicaoAdminController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @GetMapping
    public String pendentes(Model model) {
        model.addAttribute("contribuicoes", contribuicaoService.listarPendentes());
        return "financeiro-contribuicoes";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            contribuicaoService.confirmar(id);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Contribuição confirmada e lançada no financeiro.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/rejeitar")
    public String rejeitar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            contribuicaoService.rejeitar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Contribuição rejeitada.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return REDIRECT_LISTA;
    }
}