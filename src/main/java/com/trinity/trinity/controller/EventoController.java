package com.trinity.trinity.controller;

import com.trinity.trinity.model.Evento;
import com.trinity.trinity.security.Permissoes;
import com.trinity.trinity.service.EventoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public String listarEventos(Authentication authentication, Model model) {
        boolean podeGerenciar = Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_EVENTOS");

        model.addAttribute("eventos", podeGerenciar ? eventoService.listarTodos() : eventoService.listarFuturos());
        return "eventos";
    }

    @GetMapping("/eventos/novo")
    public String novoEvento(Model model) {

        model.addAttribute("evento", new Evento());

        return "forms/evento-form";
    }

    @GetMapping("/eventos/editar/{id}")
    public String editarEvento(@PathVariable("id") Long id, Model model) {

        model.addAttribute("evento", eventoService.buscarPorId(id));

        return "forms/evento-form";
    }

    @PostMapping("/eventos")
    public String salvarEvento(Evento evento) {

        eventoService.salvar(evento);

        return "redirect:/eventos";
    }

    @PostMapping("/eventos/cancelar/{id}")
    public String cancelarEvento(@PathVariable("id") Long id) {

        eventoService.cancelar(id);

        return "redirect:/eventos";
    }

    @PostMapping("/eventos/ativar/{id}")
    public String ativarEvento(@PathVariable("id") Long id) {

        eventoService.ativar(id);

        return "redirect:/eventos";
    }
}