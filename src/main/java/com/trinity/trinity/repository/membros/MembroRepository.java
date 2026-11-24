package com.trinity.trinity.repository.membros;

import com.trinity.trinity.model.membros.Membro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {

    /** Quantidade de membros ativos ou inativos. */
    long countByAtivo(boolean ativo);

    /** Verifica se já existe membro com esse CPF (só dígitos). */
    boolean existsByCpf(String cpf);

    /** Verifica se já existe membro com esse e-mail. */
    boolean existsByEmail(String email);

    /** Igual a existsByCpf, ignorando o próprio membro (usado na edição). */
    boolean existsByCpfAndIdNot(String cpf, Long id);

    /** Igual a existsByEmail, ignorando o próprio membro (usado na edição). */
    boolean existsByEmailAndIdNot(String email, Long id);

    /** Busca um membro pelo e-mail, sem diferenciar maiúsculas. */
    Optional<Membro> findByEmailIgnoreCase(String email);

    /** Busca paginada de membros por parte do nome, do CPF ou do telefone. */
    Page<Membro> findByNomeCompletoContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(
            String nome,
            String cpf,
            String telefone,
            Pageable pageable
    );
    /** Membros ativos, em ordem alfabética. */
    List<Membro> findByAtivoTrueOrderByNomeCompletoAsc();

    /** Todos os membros, em ordem alfabética. */
    List<Membro> findAllByOrderByNomeCompletoAsc();
}