package com.trinity.trinity.repository.financeiro;


import com.trinity.trinity.model.financeiro.CategoriaFinanceira;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {

    /** Verifica se já existe uma categoria com esse nome, sem diferenciar maiúsculas. */
    boolean existsByNomeIgnoreCase(String nome);

    /** Igual à anterior, ignorando a própria categoria (usado na edição). */
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    /** Todas as categorias, ordenadas por tipo e nome. */
    List<CategoriaFinanceira> findAllByOrderByTipoAscNomeAsc();

    /** Categorias ativas de um tipo, em ordem alfabética. */
    List<CategoriaFinanceira> findByTipoAndAtivoTrueOrderByNomeAsc(TipoLancamento tipo);
}