package com.trinity.trinity.repository.eventos;


import com.trinity.trinity.model.eventos.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findTop5ByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate data);
    Page<Evento> findAllByOrderByDataDescHorarioDesc(Pageable pageable);

    Page<Evento> findByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate data, Pageable pageable);

}