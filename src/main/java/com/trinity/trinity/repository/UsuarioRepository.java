package com.trinity.trinity.repository;

import com.trinity.trinity.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByMembro_EmailIgnoreCase(String email);

    boolean existsByMembroId(Long membroId);

    Optional<Usuario> findByMembroId(Long membroId);

    boolean existsByAdminTrue();
}