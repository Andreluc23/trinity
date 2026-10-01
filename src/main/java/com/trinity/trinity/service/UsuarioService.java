package com.trinity.trinity.service;

import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario salvar(Usuario usuario) {

        boolean emailDuplicado;

        if (usuario.getId() == null) {
            emailDuplicado = usuarioRepository.existsByEmail(usuario.getEmail());

            if (emailDuplicado) {
                throw new RuntimeException("E-mail já cadastrado.");
            }

            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        } else {
            emailDuplicado = usuarioRepository.existsByEmailAndIdNot(
                    usuario.getEmail(),
                    usuario.getId()
            );

            if (emailDuplicado) {
                throw new RuntimeException("E-mail já cadastrado.");
            }

            Usuario usuarioAtual = buscarPorId(usuario.getId());

            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                usuario.setSenha(usuarioAtual.getSenha());
            } else if (!usuario.getSenha().equals(usuarioAtual.getSenha())) {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }
        }

        return usuarioRepository.save(usuario);
    }

    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
}