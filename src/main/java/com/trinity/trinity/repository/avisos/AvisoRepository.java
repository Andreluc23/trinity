package com.trinity.trinity.repository.avisos;


import com.trinity.trinity.model.avisos.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    List<Aviso> findTop5ByAtivoTrueOrderByDataPublicacaoDesc();

    Page<Aviso> findAllByOrderByDataPublicacaoDesc(Pageable pageable);

    Page<Aviso> findByAtivoTrueOrderByDataPublicacaoDesc(Pageable pageable);
}