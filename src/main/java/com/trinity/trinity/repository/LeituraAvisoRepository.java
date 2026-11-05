package com.trinity.trinity.repository;

import com.trinity.trinity.model.LeituraAviso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LeituraAvisoRepository extends JpaRepository<LeituraAviso, Long> {

    boolean existsByAvisoIdAndMembroId(Long avisoId, Long membroId);

    List<LeituraAviso> findByMembroId(Long membroId);

    List<LeituraAviso> findByAvisoIdIn(Collection<Long> avisoIds);

    List<LeituraAviso> findByAvisoIdOrderByLidoEmAsc(Long avisoId);
}