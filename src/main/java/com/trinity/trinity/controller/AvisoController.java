package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Aviso;
import com.trinity.trinity.security.Permissoes;
import com.trinity.trinity.service.AvisoService;
import com.trinity.trinity.service.LeituraService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AvisoController {

    private final AvisoService avisoService;
    private final LeituraService leituraService;

    public AvisoController(AvisoService avisoService, LeituraService leituraService) {
        this.avisoService = avisoService;
        this.leituraService = leituraService;
    }

    @GetMapping("/avisos")
    public String listarAvisos(Authentication authentication, Model model) {
        boolean podeGerenciar = Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_AVISOS");
        List<Aviso> avisos = podeGerenciar ? avisoService.listarTodos() : avisoService.listarAtivos();

        model.addAttribute("avisos", avisos);
        model.addAttribute("avisosLidos", leituraService.avisosLidos(authentication.getName()));

        if (podeGerenciar) {
            model.addAttribute("leituras", leituraService.contarPorAviso(avisos));
            model.addAttribute("leitoresPossiveis", leituraService.contarLeitoresPossiveis());
        }

        return "avisos";
    }

    @GetMapping("/avisos/novo")
    public String novoAviso(Model model) {
        model.addAttribute("aviso", new Aviso());
        return "forms/aviso-form";
    }

    @GetMapping("/avisos/editar/{id}")
    public String editarAviso(@PathVariable("id") Long id, Model model) {
        model.addAttribute("aviso", avisoService.buscarPorId(id));
        return "forms/aviso-form";
    }

    @PostMapping("/avisos")
    public String salvarAviso(Aviso aviso) {
        avisoService.salvar(aviso);
        return "redirect:/avisos";
    }

    @PostMapping("/avisos/desativar/{id}")
    public String desativarAviso(@PathVariable("id") Long id) {
        avisoService.desativar(id);
        return "redirect:/avisos";
    }

    @PostMapping("/avisos/ativar/{id}")
    public String ativarAviso(@PathVariable("id") Long id) {
        avisoService.ativar(id);
        return "redirect:/avisos";
    }

    // ---------- Leitura ----------

    @PostMapping("/avisos/{id}/leitura")
    public String confirmarLeitura(@PathVariable("id") Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            leituraService.confirmar(id, authentication.getName());
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/avisos";
    }

    @GetMapping("/avisos/{id}/leituras")
    public String listarLeituras(@PathVariable("id") Long id, Model model) {
        model.addAttribute("aviso", avisoService.buscarPorId(id));
        model.addAttribute("leituras", leituraService.listarDoAviso(id));
        model.addAttribute("naoLeram", leituraService.listarQuemNaoLeu(id));
        return "aviso-leituras";
    }
}