package com.trinity.trinity.controller.membros;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.membros.Membro;
import com.trinity.trinity.model.usuarios.Usuario;
import com.trinity.trinity.service.membros.MembroService;
import com.trinity.trinity.service.usuarios.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MembroController {

    private final MembroService membroService;
    private final UsuarioService usuarioService;

    public MembroController(
            MembroService membroService,
            UsuarioService usuarioService) {

        this.membroService = membroService;
        this.usuarioService = usuarioService;
    }

    /** Lista os membros com busca e paginação, indicando quais têm acesso ao sistema. */
    @GetMapping("/membros")
    public String listarMembros(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            Model model) {

        var paginaMembros = membroService.buscar(busca, pagina);

        Set<Long> acessos = usuarioService.listarTodos()
                .stream()
                .map(Usuario::getMembro)
                .map(Membro::getId)
                .collect(Collectors.toSet());

        model.addAttribute("membros", paginaMembros.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", paginaMembros.getTotalPages());
        model.addAttribute("busca", busca);
        model.addAttribute("acessos", acessos);

        return "membros";
    }

    /** Abre o formulário de novo membro; se voltou de um erro, mantém o que foi digitado. */
    @GetMapping("/membros/novo")
    public String novoMembro(Model model) {
        if (!model.containsAttribute("membro")) {
            model.addAttribute("membro", new Membro());
        }
        return "forms/membro-form";
    }

    /** Abre o formulário de edição; se voltou de um erro, mantém o que foi digitado. */
    @GetMapping("/membros/editar/{id}")
    public String editarMembro(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("membro")) {
            model.addAttribute("membro", membroService.buscarPorId(id));
        }
        return "forms/membro-form";
    }

    /** Desativa o membro (o sistema impede desativar o único administrador). */
    @PostMapping("/membros/desativar/{id}")
    public String desativarMembro(@PathVariable("id") Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            membroService.desativar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Membro desativado.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/membros";
    }

    /** Exclui o membro e o que depende dele; é recusado se houver histórico financeiro. */
    @PostMapping("/membros/excluir/{id}")
    public String excluirMembro(@PathVariable("id") Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            membroService.excluir(id, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Membro excluído.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("falha", e.getMessage());
        }
        return "redirect:/membros";
    }

    /** Reativa o membro. */
    @PostMapping("/membros/ativar/{id}")
    public String ativarMembro(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        membroService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Membro ativado.");
        return "redirect:/membros";
    }

    /** Grava o membro; em erro de regra, volta ao formulário com o que foi digitado. */
    @PostMapping("/membros")
    public String salvarMembro(Membro membro, RedirectAttributes redirectAttributes) {
        try {
            membroService.salvar(membro);
            redirectAttributes.addFlashAttribute("sucesso", "Membro salvo com sucesso.");
            return "redirect:/membros";
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addFlashAttribute("membro", membro);

            return membro.getId() == null
                    ? "redirect:/membros/novo"
                    : "redirect:/membros/editar/" + membro.getId();
        }
    }
}
