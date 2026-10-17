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

        return "forms/escala-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {

        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaItemService.listarPorEscala(id));
        model.addAttribute("membros", membroService.listarTodos());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());

        return "forms/escala-form";
    }

    @PostMapping
    public String salvar(Escala escala) {

        Escala escalaSalva = escalaService.salvar(escala);

        return "redirect:/escalas/" + escalaSalva.getId();
    }
    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable("id") Long id) {

        escalaService.desativar(id);

        return "redirect:/escalas";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable("id") Long id) {

        escalaService.ativar(id);

        return "redirect:/escalas";
    }
    @PostMapping("/{escalaId}/integrantes")
    public String adicionarIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @RequestParam("membroId") Long membroId,
            @RequestParam("funcaoId") Long funcaoId) {

        var escala = escalaService.buscarPorId(escalaId);
        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.salvar(escala, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
    @PostMapping("/{escalaId}/integrantes/remover/{itemId}")
    public String removerIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @PathVariable("itemId") Long itemId) {

        escalaItemService.remover(itemId);

        return "redirect:/escalas/" + escalaId;
    }
    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") Long id, Model model) {

        model.addAttribute("escala", escalaService.buscarPorId(id));
        model.addAttribute("itens", escalaItemService.listarPorEscala(id));
        model.addAttribute("membros", membroService.listarTodos());
        model.addAttribute("funcoes", funcaoMusicalService.listarTodas());

        return "escala-detalhes";
    }
    @PostMapping("/{escalaId}/integrantes/editar/{itemId}")
    public String editarIntegrante(
            @PathVariable("escalaId") Long escalaId,
            @PathVariable("itemId") Long itemId,
            @RequestParam("membroId") Long membroId,
            @RequestParam("funcaoId") Long funcaoId) {

        var membro = membroService.buscarPorId(membroId);
        var funcao = funcaoMusicalService.buscarPorId(funcaoId);

        escalaItemService.atualizar(itemId, membro, funcao);

        return "redirect:/escalas/" + escalaId;
    }
}