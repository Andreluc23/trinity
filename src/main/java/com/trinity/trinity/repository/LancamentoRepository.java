package com.trinity.trinity.repository;

import com.trinity.trinity.model.Lancamento;
import com.trinity.trinity.model.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByAtivoTrueAndDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);

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
}