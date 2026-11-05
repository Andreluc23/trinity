package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.Usuario;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarioRepository;
    private final MembroRepository membroRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByMembro_EmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public Usuario buscarPorMembro(Long membroId) {
        return usuarioRepository.findByMembroId(membroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não possui conta de acesso"));
    }

    public boolean possuiAcesso(Long membroId) {
        return usuarioRepository.existsByMembroId(membroId);
    }

    public Usuario criarAcesso(Long membroId, String senhaProvisoria) {
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado."));

        if (!membro.isAtivo()) {
            throw new RegraNegocioException("Não é possível criar acesso para um membro inativo.");
        }

        if (membro.getEmail() == null || membro.getEmail().isBlank()) {
            throw new RegraNegocioException("O membro precisa ter um e-mail cadastrado.");
        }

        if (possuiAcesso(membroId)) {
            throw new RegraNegocioException("Este membro já possui uma conta de acesso.");
        }

        validarSenha(senhaProvisoria);

        Usuario usuario = new Usuario();
        usuario.setMembro(membro);
        usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
        usuario.setAtivo(true);
        usuario.setAdmin(false);

        return usuarioRepository.save(usuario);
    }

    public Usuario salvar(Usuario usuario) {
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
    public void alterarSenha(String email, String senhaAtual, String novaSenha, String confirmacao) {
        Usuario usuario = buscarPorEmail(email);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }

        validarSenha(novaSenha);

        if (!novaSenha.equals(confirmacao)) {
            throw new RegraNegocioException("A confirmação não confere com a nova senha.");
        }

        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }

        if (senha.chars().anyMatch(Character::isWhitespace)) {
            throw new RegraNegocioException("A senha não pode conter espaços.");
        }
    }
    public void redefinirSenha(Long membroId, String novaSenha) {
        Usuario usuario = buscarPorMembro(membroId);
        validarSenha(novaSenha);

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}