package com.trinity.trinity.repository;

import com.trinity.trinity.model.FuncaoMusical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncaoMusicalRepository extends JpaRepository<FuncaoMusical, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}