package com.trinity.trinity.repository.financeiro;


import com.trinity.trinity.model.financeiro.Lancamento;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, JpaSpecificationExecutor<Lancamento> {

    /** Lançamentos ativos do período, do mais recente para o mais antigo. */
    List<Lancamento> findByAtivoTrueAndDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);
    /** Verifica se a categoria já foi usada em algum lançamento. */
    boolean existsByCategoriaId(Long categoriaId);

    /** Verifica se o membro tem lançamentos registrados. */
    boolean existsByMembroId(Long membroId);

    /** Soma os valores ativos de um tipo (entrada ou saída) em um período; devolve zero se não houver nada. */
    @Query("""
            select coalesce(sum(l.valor), 0)
            from Lancamento l
            where l.ativo = true
              and l.tipo = :tipo
              and l.data between :inicio and :fim
            """)
    BigDecimal somar(@Param("tipo") TipoLancamento tipo,
                     @Param("inicio") LocalDate inicio,
                     @Param("fim") LocalDate fim);

    /** Soma os valores ativos de um tipo em todo o histórico; devolve zero se não houver nada. */
    @Query("""
        select coalesce(sum(l.valor), 0)
        from Lancamento l
        where l.ativo = true
          and l.tipo = :tipo
        """)
    BigDecimal somarTudo(@Param("tipo") TipoLancamento tipo);
}
