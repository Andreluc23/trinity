package com.trinity.trinity.controller;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Evento;
import com.trinity.trinity.model.RespostaPresenca;
import com.trinity.trinity.security.Permissoes;
import com.trinity.trinity.service.EventoService;
import com.trinity.trinity.service.PresencaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class EventoController {

    private final EventoService eventoService;
    private final PresencaService presencaService;

    public EventoController(EventoService eventoService, PresencaService presencaService) {
        this.eventoService = eventoService;
        this.presencaService = presencaService;
    }

    @GetMapping("/eventos")
    public String listarEventos(Authentication authentication, Model model) {
        boolean podeGerenciar = Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_EVENTOS");
        List<Evento> eventos = podeGerenciar ? eventoService.listarTodos() : eventoService.listarFuturos();

        model.addAttribute("eventos", eventos);
        model.addAttribute("minhasRespostas", presencaService.respostasDoMembro(authentication.getName()));
        model.addAttribute("hoje", LocalDate.now());

        if (podeGerenciar) {
            model.addAttribute("contagens", presencaService.contarPorEvento(eventos));
        }

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
    public String salvarEvento(Evento evento, RedirectAttributes redirectAttributes) {
        eventoService.salvar(evento);
        redirectAttributes.addFlashAttribute("sucesso", "Evento salvo com sucesso.");
        return "redirect:/eventos";
    }

    @PostMapping("/eventos/cancelar/{id}")
    public String cancelarEvento(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        eventoService.cancelar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Evento cancelado.");
        return "redirect:/eventos";
    }

    @PostMapping("/eventos/ativar/{id}")
    public String ativarEvento(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        eventoService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Evento reativado.");
        return "redirect:/eventos";
    }

    // ---------- Presença ----------

    @PostMapping("/eventos/{id}/presenca")
    public String responderPresenca(@PathVariable("id") Long id,
                                    @RequestParam(name = "resposta", required = false) RespostaPresenca resposta,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            presencaService.responder(id, authentication.getName(), resposta);
            redirectAttributes.addFlashAttribute("sucesso", "Sua resposta foi registrada.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/eventos";
    }

    @GetMapping("/eventos/{id}/presencas")
    public String listarPresencas(@PathVariable("id") Long id, Model model) {
        model.addAttribute("evento", eventoService.buscarPorId(id));
        model.addAttribute("presencas", presencaService.listarDoEvento(id));
        return "evento-presencas";
    }
    @PostMapping("/eventos/excluir/{id}")
    public String excluirEvento(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        eventoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Evento excluído.");
        return "redirect:/eventos";
    }
}