package com.trinity.trinity.repository;

import com.trinity.trinity.model.Patrimonio;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface PatrimonioRepository extends JpaRepository<Patrimonio, Long> {
    Page<Patrimonio> findByNomeContainingIgnoreCaseOrCategoriaContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(
            String nome,
            String categoria,
            String localizacao,
            Pageable pageable
    );
}