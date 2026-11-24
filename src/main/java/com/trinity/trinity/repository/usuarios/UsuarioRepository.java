package com.trinity.trinity.repository.usuarios;


import com.trinity.trinity.model.usuarios.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca o usuário pelo e-mail do seu membro (é o que o login usa). */
    Optional<Usuario> findByMembro_EmailIgnoreCase(String email);

    /** Verifica se o membro tem conta de acesso. */
    boolean existsByMembroId(Long membroId);

    /** Busca a conta de acesso de um membro. */
    Optional<Usuario> findByMembroId(Long membroId);

    /** Verifica se já existe algum administrador. */
    boolean existsByAdminTrue();
    /** Conta os administradores ativos (usuário e membro ativos); evita ficar sem nenhum. */
    long countByAdminTrueAndAtivoTrueAndMembro_AtivoTrue();
}