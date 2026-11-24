package com.trinity.trinity.repository.eventos;


import com.trinity.trinity.model.eventos.PresencaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PresencaEventoRepository extends JpaRepository<PresencaEvento, Long> {

    /** Resposta de presença de um membro a um evento, se existir. */
    Optional<PresencaEvento> findByEventoIdAndMembroId(Long eventoId, Long membroId);

    /** Respostas de presença de um membro. */
    List<PresencaEvento> findByMembroId(Long membroId);

    /** Respostas de vários eventos de uma vez (para contar presenças na lista). */
    List<PresencaEvento> findByEventoIdIn(Collection<Long> eventoIds);

    /** Respostas de um evento, em ordem alfabética do nome do membro. */
    List<PresencaEvento> findByEventoIdOrderByMembroNomeCompletoAsc(Long eventoId);
    /** Apaga as respostas de um evento (usado ao excluí-lo). */
    void deleteByEventoId(Long eventoId);

    /** Apaga as respostas de um membro (usado ao excluí-lo). */
    void deleteByMembroId(Long membroId);
}