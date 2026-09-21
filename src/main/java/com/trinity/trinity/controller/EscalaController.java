package com.trinity.trinity.controller;

import com.trinity.trinity.model.Escala;
import com.trinity.trinity.service.EscalaItemService;
import com.trinity.trinity.service.EscalaService;
import com.trinity.trinity.service.FuncaoMusicalService;
import com.trinity.trinity.service.MembroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/escalas")
public class EscalaController {

    private final EscalaService escalaService;
    private final EscalaItemService escalaItemService;
    private final MembroService membroService;
    private final FuncaoMusicalService funcaoMusicalService;

    public EscalaController(
            EscalaService escalaService,
            EscalaItemService escalaItemService,
            MembroService membroService,
            FuncaoMusicalService funcaoMusicalService) {

        this.escalaService = escalaService;
        this.escalaItemService = escalaItemService;
        this.membroService = membroService;
        this.funcaoMusicalService = funcaoMusicalService;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute("escalas", escalaService.listarTodas());

        return "escalas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {

        model.addAttribute("escala", new Escala());

        return "escala-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaItemService.listarPorEscala(id));
        model.addAttribute("membros", membroService.listarTodos());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());

        return "escala-form";
    }

    @PostMapping
    public String salvar(Escala escala) {

        Escala escalaSalva = escalaService.salvar(escala);

        return "redirect:/escalas/" + escalaSalva.getId();
    }
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable Long id) {

        escalaService.desativar(id);

        return "redirect:/escalas";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id) {

        escalaService.ativar(id);

        return "redirect:/escalas";
    }
    @PostMapping("/{escalaId}/integrantes")
    public String adicionarIntegrante(
            @PathVariable Long escalaId,
            @RequestParam Long membroId,
            @RequestParam Long funcaoId) {

        var escala = escalaService.buscarPorId(escalaId);
        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.salvar(escala, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
    @PostMapping("/{escalaId}/integrantes/remover/{itemId}")
    public String removerIntegrante(
            @PathVariable Long escalaId,
            @PathVariable Long itemId) {

        escalaItemService.remover(itemId);

        return "redirect:/escalas/" + escalaId;
    }
    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {

        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaItemService.listarPorEscala(id));
        model.addAttribute("membros", membroService.listarTodos());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());

        return "escala-detalhes";
    }
    @PostMapping("/{escalaId}/integrantes/editar/{itemId}")
    public String editarIntegrante(
            @PathVariable Long escalaId,
            @PathVariable Long itemId,
            @RequestParam Long membroId,
            @RequestParam Long funcaoId) {

        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.atualizar(itemId, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
}