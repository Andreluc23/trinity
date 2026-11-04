package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.service.FuncaoMusicalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/funcoes-musicais")
public class FuncaoMusicalController {

    private static final String FORMULARIO = "forms/funcao-musical-form";

    private final FuncaoMusicalService funcaoMusicalService;

    public FuncaoMusicalController(FuncaoMusicalService funcaoMusicalService) {
        this.funcaoMusicalService = funcaoMusicalService;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("funcaoMusical", funcaoMusicalService.buscarPorId(id));
        return FORMULARIO;
    }

    @PostMapping
    public String salvar(@ModelAttribute("funcaoMusical") FuncaoMusical funcaoMusical,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            funcaoMusicalService.salvar(funcaoMusical);
            redirectAttributes.addFlashAttribute("sucessoFuncao", "Função salva com sucesso.");
        } catch (RegraNegocioException e) {
            if (funcaoMusical.getId() != null) {
                model.addAttribute("erro", e.getMessage());
                return FORMULARIO;
            }
            redirectAttributes.addFlashAttribute("erroFuncao", e.getMessage());
        }
        return voltarComModalAberto(redirectAttributes);
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        funcaoMusicalService.desativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        funcaoMusicalService.ativar(id);
        return voltarComModalAberto(redirectAttributes);
    }

    private String voltarComModalAberto(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("abrirFuncoes", true);
        return "redirect:/escalas";
    }
}