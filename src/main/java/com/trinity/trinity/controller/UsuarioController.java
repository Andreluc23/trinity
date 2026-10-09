
package com.trinity.trinity.controller;

import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.service.MembroService;
import com.trinity.trinity.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String listar() {
        return "redirect:/membros";
    }

    @GetMapping("/criar-acesso/{id}")
    public String formularioCriarAcesso(
            @PathVariable Long id,
            Model model) {

        if (usuarioService.possuiAcesso(id)) {
            return "redirect:/usuarios/permissoes/" + id;
        }

        model.addAttribute("membro", membroService.buscarPorId(id));

        return "forms/criar-acesso-form";
    }

    @PostMapping("/criar-acesso/{id}")
    public String criarAcesso(
            @PathVariable Long id,
            @RequestParam String senhaProvisoria,
            Model model) {

        try {
            usuarioService.criarAcesso(id, senhaProvisoria);
            return "redirect:/membros";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("membro", membroService.buscarPorId(id));

            return "forms/criar-acesso-form";
        }
    }

    @GetMapping("/permissoes/{id}")
    public String permissoes(
            @PathVariable Long id,
            Model model) {

        Usuario usuario = usuarioService.buscarPorMembro(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("membro", usuario.getMembro());

        return "forms/permissoes-form";
    }

    @PostMapping("/permissoes/{id}")
    public String salvarPermissoes(
            @PathVariable Long id,
            @ModelAttribute Usuario formulario) {

        Usuario usuario = usuarioService.buscarPorMembro(id);

        usuario.setGerenciarMembros(formulario.isGerenciarMembros());
        usuario.setGerenciarAvisos(formulario.isGerenciarAvisos());
        usuario.setGerenciarEventos(formulario.isGerenciarEventos());
        usuario.setAcessoEscalas(formulario.isAcessoEscalas());
        usuario.setGerenciarEscalas(formulario.isGerenciarEscalas());
        usuario.setGerenciarPatrimonio(formulario.isGerenciarPatrimonio());
        usuario.setGerenciarFinanceiro(formulario.isGerenciarFinanceiro());

        usuarioService.salvar(usuario);

        return "redirect:/membros";
    }
}
