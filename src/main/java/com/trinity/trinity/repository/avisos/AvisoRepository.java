package com.trinity.trinity.repository.avisos;


import com.trinity.trinity.model.avisos.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    /** Os 5 avisos ativos mais recentes (card do dashboard). */
    List<Aviso> findTop5ByAtivoTrueOrderByDataPublicacaoDesc();

    /** Todos os avisos, do mais recente para o mais antigo, paginados. */
    Page<Aviso> findAllByOrderByDataPublicacaoDesc(Pageable pageable);

    /** Só os avisos ativos, do mais recente para o mais antigo, paginados. */
    Page<Aviso> findByAtivoTrueOrderByDataPublicacaoDesc(Pageable pageable);
}