package com.trinity.trinity.repository;

import com.trinity.trinity.model.Escala;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long> {
    List<Escala> findAllByOrderByDataDescHorarioDesc();
    List<Escala> findAllByOrderByDataAscHorarioAsc();
    /**
     * Próximas escalas primeiro (da mais próxima para a mais distante),
     * depois as que já passaram (da mais recente para a mais antiga).
     */
    @Query(value = """
        select e from Escala e
        order by case when e.data >= :hoje then 0 else 1 end,
                 case when e.data >= :hoje then e.data end asc,
                 case when e.data < :hoje then e.data end desc,
                 e.horario asc
        """,
            countQuery = "select count(e) from Escala e")
    Page<Escala> listarProximasPrimeiro(@Param("hoje") LocalDate hoje, Pageable pageable);
}