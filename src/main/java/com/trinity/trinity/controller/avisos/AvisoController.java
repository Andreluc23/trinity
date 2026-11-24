package com.trinity.trinity.controller.avisos;

import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.avisos.Aviso;
import com.trinity.trinity.security.Permissoes;
import com.trinity.trinity.service.avisos.AvisoService;
import com.trinity.trinity.service.avisos.LeituraService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /** Lista os avisos com paginação: quem gerencia vê todos, os demais só os ativos, e cada um vê quais já leu. */
    @GetMapping("/avisos")
    public String listarAvisos(@RequestParam(name = "pagina", defaultValue = "0") int pagina,
                               Authentication authentication,
                               Model model) {
        boolean podeGerenciar = Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_AVISOS");
        Page<Aviso> paginaAvisos = podeGerenciar ? avisoService.listarTodos(pagina) : avisoService.listarAtivos(pagina);
        List<Aviso> avisos = paginaAvisos.getContent();

        model.addAttribute("avisos", avisos);
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", paginaAvisos.getTotalPages());
        model.addAttribute("avisosLidos", leituraService.avisosLidos(authentication.getName()));

        if (podeGerenciar) {
            model.addAttribute("leituras", leituraService.contarPorAviso(avisos));
            model.addAttribute("leitoresPossiveis", leituraService.contarLeitoresPossiveis());
        }

        return "avisos";
    }

    /** Abre o formulário para cadastrar um aviso. */
    @GetMapping("/avisos/novo")
    public String novoAviso(Model model) {
        model.addAttribute("aviso", new Aviso());
        return "forms/aviso-form";
    }

    /** Abre o formulário preenchido para editar um aviso. */
    @GetMapping("/avisos/editar/{id}")
    public String editarAviso(@PathVariable("id") Long id, Model model) {
        model.addAttribute("aviso", avisoService.buscarPorId(id));
        return "forms/aviso-form";
    }

    /** Grava o aviso (novo ou editado) e volta para a lista. */
    @PostMapping("/avisos")
    public String salvarAviso(Aviso aviso, RedirectAttributes redirectAttributes) {
        avisoService.salvar(aviso);
        redirectAttributes.addFlashAttribute("sucesso", "Aviso salvo com sucesso.");
        return "redirect:/avisos";
    }

    /** Desativa o aviso, escondendo-o dos membros sem apagá-lo. */
    @PostMapping("/avisos/desativar/{id}")
    public String desativarAviso(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        avisoService.desativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Aviso desativado.");
        return "redirect:/avisos";
    }

    /** Reativa um aviso desativado. */
    @PostMapping("/avisos/ativar/{id}")
    public String ativarAviso(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        avisoService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Aviso ativado.");
        return "redirect:/avisos";
    }

    // ---------- Leitura ----------

    /** Registra que o membro logado leu o aviso. */
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

    /** Mostra quem já leu o aviso e quem ainda não leu. */
    @GetMapping("/avisos/{id}/leituras")
    public String listarLeituras(@PathVariable("id") Long id, Model model) {
        model.addAttribute("aviso", avisoService.buscarPorId(id));
        model.addAttribute("leituras", leituraService.listarDoAviso(id));
        model.addAttribute("naoLeram", leituraService.listarQuemNaoLeu(id));
        return "aviso-leituras";
    }
    /** Exclui o aviso junto com as confirmações de leitura dele. */
    @PostMapping("/avisos/excluir/{id}")
    public String excluirAviso(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        avisoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Aviso excluído.");
        return "redirect:/avisos";
    }
}