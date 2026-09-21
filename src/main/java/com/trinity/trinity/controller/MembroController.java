package com.trinity.trinity.controller;

import com.trinity.trinity.model.Membro;
import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MembroController {

    private final MembroService membroService;

    public MembroController(MembroService membroService) {
        this.membroService = membroService;
    }

    @GetMapping("/membros")
    public String listarMembros(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        var paginaMembros = membroService.buscar(busca, pagina);

        model.addAttribute("membros", paginaMembros.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", paginaMembros.getTotalPages());
        model.addAttribute("busca", busca);

        return "membros";
    }

    @GetMapping("/membros/novo")
    public String novoMembro(Model model) {
        model.addAttribute("membro", new Membro());
        return "forms/membro-form";
    }

    @GetMapping("/membros/editar/{id}")
    public String editarMembro(@PathVariable Long id, Model model) {
        model.addAttribute("membro", membroService.buscarPorId(id));
        return "forms/membro-form";
    }
    @PostMapping("/membros/desativar/{id}")
    public String desativarMembro(@PathVariable Long id) {

        membroService.desativar(id);

        return "redirect:/membros";
    }

    @PostMapping("/membros")
    public String salvarMembro(Membro membro, Model model) {

        try {
            membroService.salvar(membro);
            return "redirect:/membros";

        } catch (RuntimeException e) {

            model.addAttribute("erro", e.getMessage());
            model.addAttribute("membro", membro);

            return "forms/membro-form";
        }
    }
    @PostMapping("/membros/ativar/{id}")
    public String ativarMembro(@PathVariable Long id) {

        membroService.ativar(id);

        return "redirect:/membros";
    }
}