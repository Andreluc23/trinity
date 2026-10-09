
package com.trinity.trinity.service;

import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MembroRepository membroRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            MembroRepository membroRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.membroRepository = membroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByMembro_Email(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));
    }

    public Usuario buscarPorMembro(Long membroId) {
        return usuarioRepository.findAll()
                .stream()
                .filter(usuario ->
                        usuario.getMembro().getId().equals(membroId))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Membro não possui conta de acesso"));
    }

    public boolean possuiAcesso(Long membroId) {
        return usuarioRepository.existsByMembroId(membroId);
    }

    public Usuario criarAcesso(Long membroId, String senhaProvisoria) {

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Membro não encontrado."));

        if (!membro.isAtivo()) {
            throw new IllegalArgumentException(
                    "Não é possível criar acesso para um membro inativo.");
        }

        if (membro.getEmail() == null ||
                membro.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "O membro precisa ter um e-mail cadastrado.");
        }

        if (possuiAcesso(membroId)) {
            throw new IllegalArgumentException(
                    "Este membro já possui uma conta de acesso.");
        }

        if (senhaProvisoria == null ||
                senhaProvisoria.isBlank() ||
                senhaProvisoria.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha provisória deve ter pelo menos 8 caracteres.");
        }

        Usuario usuario = new Usuario();
        usuario.setMembro(membro);
        usuario.setSenha(senhaProvisoria);
        usuario.setAtivo(true);
        usuario.setAdmin(false);

        return salvar(usuario);
    }

    public Usuario salvar(Usuario usuario) {

        if (usuario.getId() == null) {

            if (usuario.getMembro() == null ||
                    usuario.getMembro().getId() == null) {
                throw new IllegalArgumentException("Selecione um membro.");
            }

            Long membroId = usuario.getMembro().getId();

            if (usuarioRepository.existsByMembroId(membroId)) {
                throw new IllegalArgumentException(
                        "Este membro já possui uma conta de acesso.");
            }

            if (usuario.getSenha() == null ||
                    usuario.getSenha().isBlank()) {
                throw new IllegalArgumentException(
                        "A senha é obrigatória.");
            }

            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        } else {

            Usuario usuarioAtual = buscarPorId(usuario.getId());

            usuario.setMembro(usuarioAtual.getMembro());
            usuario.setSenha(usuarioAtual.getSenha());
            usuario.setAtivo(usuarioAtual.isAtivo());
            usuario.setAdmin(usuarioAtual.isAdmin());
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
