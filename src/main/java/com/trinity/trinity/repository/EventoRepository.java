package com.trinity.trinity.repository;

import com.trinity.trinity.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findTop5ByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate data);

}