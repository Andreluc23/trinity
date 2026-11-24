package com.trinity.trinity.service.usuarios;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.membros.Membro;

import com.trinity.trinity.model.usuarios.Usuario;
import com.trinity.trinity.repository.membros.MembroRepository;
import com.trinity.trinity.repository.usuarios.UsuarioRepository;
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

    /** Todos os usuários (contas de acesso). */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /** Busca um usuário; se não existir, lança erro de recurso não encontrado. */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    /** Busca o usuário pelo e-mail do membro; se não existir, lança erro de recurso não encontrado. */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByMembro_EmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    /** Busca a conta de acesso de um membro; se ele não tiver, lança erro de recurso não encontrado. */
    public Usuario buscarPorMembro(Long membroId) {
        return usuarioRepository.findByMembroId(membroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não possui conta de acesso"));
    }

    /** Informa se o membro tem conta de acesso. */
    public boolean possuiAcesso(Long membroId) {
        return usuarioRepository.existsByMembroId(membroId);
    }

    /** Cria a conta do membro (precisa estar ativo, ter e-mail e senha válida), com a senha criptografada. */
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

    /** Grava as alterações de um usuário. */
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Concede ou retira o papel de administrador; impede retirar o próprio acesso ou do último administrador
     * ativo.
     */
    public void definirAdmin(Long membroId, boolean admin, String emailLogado) {
        Usuario usuario = buscarPorMembro(membroId);

        if (usuario.isAdmin() == admin) {
            return;
        }

        if (!admin) {
            String emailUsuario = usuario.getMembro().getEmail();

            if (emailUsuario != null && emailUsuario.equalsIgnoreCase(emailLogado)) {
                throw new RegraNegocioException("Você não pode remover o seu próprio acesso de administrador.");
            }

            boolean ultimoAdminAtivo = usuario.isAtivo()
                    && usuarioRepository.countByAdminTrueAndAtivoTrueAndMembro_AtivoTrue() <= 1;

            if (ultimoAdminAtivo) {
                throw new RegraNegocioException("Não é possível remover o único administrador ativo do sistema.");
            }
        }

        usuario.setAdmin(admin);
        usuarioRepository.save(usuario);
    }

    /** Desativa a conta de acesso. */
    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    /** Ativa a conta de acesso. */
    public void ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
    /** Troca a senha do usuário: confere a atual, valida a nova, a confirmação e se é diferente da anterior. */
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

    /** Exige pelo menos 8 caracteres e nenhum espaço. */
    private void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }

        if (senha.chars().anyMatch(Character::isWhitespace)) {
            throw new RegraNegocioException("A senha não pode conter espaços.");
        }
    }
    /** O administrador define uma nova senha para o usuário de um membro. */
    public void redefinirSenha(Long membroId, String novaSenha) {
        Usuario usuario = buscarPorMembro(membroId);
        validarSenha(novaSenha);

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}