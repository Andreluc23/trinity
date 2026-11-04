package com.trinity.trinity.repository;

import com.trinity.trinity.model.FuncaoMusical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncaoMusicalRepository extends JpaRepository<FuncaoMusical, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<FuncaoMusical> findAllByOrderByNomeAsc();

    List<FuncaoMusical> findByAtivoTrueOrderByNomeAsc();
}