package com.trinity.trinity.repository;

import com.trinity.trinity.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long> {
    List<Escala> findAllByOrderByDataDescHorarioDesc();
}