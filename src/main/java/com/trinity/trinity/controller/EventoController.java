package com.trinity.trinity.controller;

import com.trinity.trinity.model.Evento;
import com.trinity.trinity.service.EventoService;
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
    public String listarEventos(Model model) {

        model.addAttribute("eventos", eventoService.listarTodos());

        return "eventos";
    }

    @GetMapping("/eventos/novo")
    public String novoEvento(Model model) {

        model.addAttribute("evento", new Evento());

        return "forms/evento-form";
    }

    @GetMapping("/eventos/editar/{id}")
    public String editarEvento(@PathVariable Long id, Model model) {

        model.addAttribute("evento", eventoService.buscarPorId(id));

        return "forms/evento-form";
    }

    @PostMapping("/eventos")
    public String salvarEvento(Evento evento) {

        eventoService.salvar(evento);

        return "redirect:/eventos";
    }

    @PostMapping("/eventos/cancelar/{id}")
    public String cancelarEvento(@PathVariable Long id) {

        eventoService.cancelar(id);

        return "redirect:/eventos";
    }

    @PostMapping("/eventos/ativar/{id}")
    public String ativarEvento(@PathVariable Long id) {

        eventoService.ativar(id);

        return "redirect:/eventos";
    }
}