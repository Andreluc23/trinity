package com.trinity.trinity.repository;

import com.trinity.trinity.model.Contribuicao;
import com.trinity.trinity.model.StatusContribuicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {

    List<Contribuicao> findByMembroIdOrderByCriadaEmDesc(Long membroId);

    List<Contribuicao> findByStatusInOrderByCriadaEmAsc(Collection<StatusContribuicao> status);

    long countByStatus(StatusContribuicao status);

}
