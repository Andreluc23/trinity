package com.trinity.trinity.repository;

import com.trinity.trinity.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    List<Aviso> findTop5ByAtivoTrueOrderByDataPublicacaoDesc();
}