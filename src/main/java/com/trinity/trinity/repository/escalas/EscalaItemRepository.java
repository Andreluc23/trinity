package com.trinity.trinity.repository.escalas;

import com.trinity.trinity.model.escalas.EscalaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaItemRepository extends JpaRepository<EscalaItem, Long> {

    /** Integrantes de uma escala, em ordem alfabética de função. */
    List<EscalaItem> findByEscalaIdOrderByFuncaoMusicalNomeAsc(Long escalaId);

    /** Apaga os integrantes de uma escala. */
    void deleteByEscalaId(Long escalaId);

    /** Verifica se a função já foi usada em alguma escala. */
    boolean existsByFuncaoMusicalId(Long funcaoMusicalId);

    /** Remove um membro de todas as escalas (usado ao excluí-lo). */
    void deleteByMembroId(Long membroId);
}