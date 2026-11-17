package com.trinity.trinity.repository.eventos;


import com.trinity.trinity.model.eventos.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findTop5ByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate data);
    Page<Evento> findAllByOrderByDataDescHorarioDesc(Pageable pageable);

    Page<Evento> findByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate data, Pageable pageable);
    @Query("""
        select e from Evento e
        order by case when e.data >= :hoje then 0 else 1 end,
                 case when e.data >= :hoje then e.data end asc,
                 case when e.data >= :hoje then e.horario end asc,
                 case when e.data < :hoje then e.data end desc,
                 case when e.data < :hoje then e.horario end desc
        """)
    Page<Evento> listarProximosPrimeiro(@Param("hoje") LocalDate hoje, Pageable pageable);

}