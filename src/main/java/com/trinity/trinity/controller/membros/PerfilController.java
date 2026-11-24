package com.trinity.trinity.controller.membros;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.service.usuarios.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Mostra o perfil do membro logado. */
    @GetMapping
    public String exibir(Authentication authentication, Model model) {
        model.addAttribute("membro", usuarioService.buscarPorEmail(authentication.getName()).getMembro());
        return "perfil";
    }
    /** Troca a senha do usuário logado, exigindo a senha atual e a confirmação da nova. */
    @PostMapping("/senha")
    public String alterarSenha(Authentication authentication,
                               @RequestParam("senhaAtual") String senhaAtual,
                               @RequestParam("novaSenha") String novaSenha,
                               @RequestParam("confirmacao") String confirmacao,
                               RedirectAttributes redirectAttributes) {
        try {
            usuarioService.alterarSenha(authentication.getName(), senhaAtual, novaSenha, confirmacao);
            redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/perfil";
    }
}