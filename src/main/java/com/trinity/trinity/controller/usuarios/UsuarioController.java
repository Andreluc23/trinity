
package com.trinity.trinity.controller.usuarios;

import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.usuarios.Usuario;
import com.trinity.trinity.service.membros.MembroService;
import com.trinity.trinity.service.usuarios.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MembroService membroService;

    public UsuarioController(
            UsuarioService usuarioService,
            MembroService membroService) {

        this.usuarioService = usuarioService;
        this.membroService = membroService;
    }

    /** Redireciona para a lista de membros, onde os acessos são gerenciados. */
    @GetMapping
    public String listar() {
        return "redirect:/membros";
    }

    /** Abre o formulário para criar o acesso de um membro; se ele já tem acesso, vai para as permissões. */
    @GetMapping("/criar-acesso/{id}")
    public String formularioCriarAcesso(
            @PathVariable("id") Long id,
            Model model) {

        if (usuarioService.possuiAcesso(id)) {
            return "redirect:/usuarios/permissoes/" + id;
        }

        model.addAttribute("membro", membroService.buscarPorId(id));

        return "forms/criar-acesso-form";
    }

    /** Cria o acesso do membro com uma senha provisória. */
    @PostMapping("/criar-acesso/{id}")
    public String criarAcesso(@PathVariable("id") Long id,
                              @RequestParam("senhaProvisoria") String senhaProvisoria,
                              RedirectAttributes redirectAttributes) {
        try {
            usuarioService.criarAcesso(id, senhaProvisoria);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Acesso criado. Informe a senha provisória ao membro.");
            return "redirect:/membros";
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios/criar-acesso/" + id;
        }
    }

    /** Abre a tela de permissões do usuário de um membro. */
    @GetMapping("/permissoes/{id}")
    public String permissoes(
            @PathVariable("id") Long id,
            Model model) {

        Usuario usuario = usuarioService.buscarPorMembro(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("membro", usuario.getMembro());

        return "forms/permissoes-form";
    }

    /** Salva as permissões; só copia os campos de permissão do formulário e protege o único administrador. */
    @PostMapping("/permissoes/{id}")
    public String salvarPermissoes(@PathVariable("id") Long id,
                                   @ModelAttribute Usuario formulario,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.definirAdmin(id, formulario.isAdmin(), authentication.getName());
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios/permissoes/" + id;
        }

        Usuario usuario = usuarioService.buscarPorMembro(id);

        usuario.setGerenciarMembros(formulario.isGerenciarMembros());
        usuario.setGerenciarAvisos(formulario.isGerenciarAvisos());
        usuario.setGerenciarEventos(formulario.isGerenciarEventos());
        usuario.setAcessoEscalas(formulario.isAcessoEscalas());
        usuario.setGerenciarEscalas(formulario.isGerenciarEscalas());
        usuario.setGerenciarPatrimonio(formulario.isGerenciarPatrimonio());
        usuario.setGerenciarFinanceiro(formulario.isGerenciarFinanceiro());

        if (usuario.isAdmin()) {
            usuario.concederTodasAsPermissoes();
        }

        usuarioService.salvar(usuario);

        redirectAttributes.addFlashAttribute("sucesso", "Permissões atualizadas.");
        return "redirect:/membros";
    }
    /** Define uma nova senha provisória para o usuário de um membro. */
    @PostMapping("/redefinir-senha/{id}")
    public String redefinirSenha(@PathVariable("id") Long id,
                                 @RequestParam("novaSenha") String novaSenha,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirSenha(id, novaSenha);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Senha redefinida. Informe a nova senha ao membro.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios/permissoes/" + id;
    }
}
