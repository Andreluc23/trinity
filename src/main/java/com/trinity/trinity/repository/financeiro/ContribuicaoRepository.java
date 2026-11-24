package com.trinity.trinity.repository.financeiro;


import com.trinity.trinity.model.financeiro.Contribuicao;
import com.trinity.trinity.model.financeiro.StatusContribuicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {

    /** Contribuições de um membro, da mais recente para a mais antiga. */
    List<Contribuicao> findByMembroIdOrderByCriadaEmDesc(Long membroId);

    /** Contribuições nos status informados, da mais antiga para a mais recente. */
    List<Contribuicao> findByStatusInOrderByCriadaEmAsc(Collection<StatusContribuicao> status);

    /** Quantidade de contribuições em um status. */
    long countByStatus(StatusContribuicao status);
    /** Verifica se a categoria já foi usada em alguma contribuição. */
    boolean existsByCategoriaId(Long categoriaId);

    /** Verifica se o membro tem contribuições registradas. */
    boolean existsByMembroId(Long membroId);

}
