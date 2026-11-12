package com.trinity.trinity.repository;

import com.trinity.trinity.model.PresencaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PresencaEventoRepository extends JpaRepository<PresencaEvento, Long> {

    Optional<PresencaEvento> findByEventoIdAndMembroId(Long eventoId, Long membroId);

    List<PresencaEvento> findByMembroId(Long membroId);

    List<PresencaEvento> findByEventoIdIn(Collection<Long> eventoIds);

    List<PresencaEvento> findByEventoIdOrderByMembroNomeCompletoAsc(Long eventoId);
    void deleteByEventoId(Long eventoId);
}