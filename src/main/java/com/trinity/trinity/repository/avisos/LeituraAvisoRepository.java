package com.trinity.trinity.repository.avisos;


import com.trinity.trinity.model.avisos.LeituraAviso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LeituraAvisoRepository extends JpaRepository<LeituraAviso, Long> {

    /** Verifica se o membro já confirmou a leitura do aviso. */
    boolean existsByAvisoIdAndMembroId(Long avisoId, Long membroId);

    /** Leituras confirmadas por um membro. */
    List<LeituraAviso> findByMembroId(Long membroId);

    /** Leituras de vários avisos de uma vez (para contar leitores na lista). */
    List<LeituraAviso> findByAvisoIdIn(Collection<Long> avisoIds);

    /** Leituras de um aviso, da mais antiga para a mais recente. */
    List<LeituraAviso> findByAvisoIdOrderByLidoEmAsc(Long avisoId);
    /** Apaga as leituras de um aviso (usado ao excluí-lo). */
    void deleteByAvisoId(Long avisoId);

    /** Apaga as leituras de um membro (usado ao excluí-lo). */
    void deleteByMembroId(Long membroId);
}