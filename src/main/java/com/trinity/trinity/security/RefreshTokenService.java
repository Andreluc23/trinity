package com.trinity.trinity.security;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.model.usuarios.RefreshToken;
import com.trinity.trinity.model.usuarios.Usuario;
import com.trinity.trinity.repository.usuarios.RefreshTokenRepository;
import com.trinity.trinity.repository.usuarios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final Duration validade;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UsuarioRepository usuarioRepository,
                               @Value("${trinity.jwt.refresh-validade-dias}") long validadeDias) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.validade = Duration.ofDays(validadeDias);
    }

    public Duration getValidade() {
        return validade;
    }

    /** Cria e salva um refresh token novo para o usuário. */
    @Transactional
    public RefreshToken criar(String email) {
        Usuario usuario = usuarioRepository.findByMembro_EmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        return refreshTokenRepository.save(new RefreshToken(usuario, validade));
    }
    /**
     * Consome o refresh token (uso único) e devolve um novo, se ele existir, não tiver expirado e a conta
     * estiver ativa.
     */
    @Transactional
    public Optional<RefreshToken> renovar(String token) {
        return refreshTokenRepository.findByToken(token)
                .flatMap(atual -> {
                    refreshTokenRepository.delete(atual);

                    if (atual.isExpirado() || !contaAtiva(atual.getUsuario())) {
                        return Optional.empty();
                    }

                    return Optional.of(refreshTokenRepository.save(new RefreshToken(atual.getUsuario(), validade)));
                });
    }
    /** Informa se o usuário e o membro dele estão ativos. */
    private boolean contaAtiva(Usuario usuario) {
        return usuario.isAtivo() && usuario.getMembro().isAtivo();
    }
    /** Apaga o refresh token do banco. */
    @Transactional
    public void revogar(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}