package com.trinity.trinity.repository.usuarios;


import com.trinity.trinity.model.usuarios.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Busca um refresh token pelo valor guardado no cookie. */
    Optional<RefreshToken> findByToken(String token);

    /** Apaga os refresh tokens de um usuário (usado ao excluir o membro). */
    void deleteByUsuarioId(Long usuarioId);
}