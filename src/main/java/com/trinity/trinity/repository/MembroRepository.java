package com.trinity.trinity.repository;

import com.trinity.trinity.model.Membro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {

    long countByAtivo(boolean ativo);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Membro> findByEmailIgnoreCase(String email);

    Page<Membro> findByNomeCompletoContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(
            String nome,
            String cpf,
            String telefone,
            Pageable pageable
    );
    List<Membro> findByAtivoTrueOrderByNomeCompletoAsc();
}