package com.trinity.trinity.repository.usuarios;


import com.trinity.trinity.model.usuarios.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}