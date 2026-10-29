package com.trinity.trinity.repository;

import com.trinity.trinity.model.CategoriaFinanceira;
import com.trinity.trinity.model.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<CategoriaFinanceira> findAllByOrderByTipoAscNomeAsc();

    List<CategoriaFinanceira> findByTipoAndAtivoTrueOrderByNomeAsc(TipoLancamento tipo);
}