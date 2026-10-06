package com.trinity.trinity.controller;

import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "forms/usuario-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);

        // Não enviamos a senha criptografada para o formulário
        usuario.setSenha("");

        model.addAttribute("usuario", usuario);
        return "forms/usuario-form";
    }

    @PostMapping
    public String salvar(@ModelAttribute Usuario usuario) {
        usuarioService.salvar(usuario);
        return "redirect:/usuarios";
    }

    @PostMapping("/desativar/{id}")
    public String desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return "redirect:/usuarios";
    }

    @PostMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id) {
        usuarioService.ativar(id);
        return "redirect:/usuarios";
    }
}