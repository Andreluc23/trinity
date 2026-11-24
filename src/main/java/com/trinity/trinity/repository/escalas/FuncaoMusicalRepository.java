package com.trinity.trinity.repository.escalas;


import com.trinity.trinity.model.escalas.FuncaoMusical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncaoMusicalRepository extends JpaRepository<FuncaoMusical, Long> {

    /** Verifica se já existe uma função com esse nome, sem diferenciar maiúsculas. */
    boolean existsByNomeIgnoreCase(String nome);

    /** Igual à anterior, ignorando a própria função (usado na edição). */
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    /** Todas as funções, em ordem alfabética. */
    List<FuncaoMusical> findAllByOrderByNomeAsc();

    /** Só as funções ativas, em ordem alfabética. */
    List<FuncaoMusical> findByAtivoTrueOrderByNomeAsc();
}