package com.trinity.trinity.repository.escalas;

import com.trinity.trinity.model.escalas.EscalaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaItemRepository extends JpaRepository<EscalaItem, Long> {

    List<EscalaItem> findByEscalaIdOrderByFuncaoMusicalNomeAsc(Long escalaId);

    void deleteByEscalaId(Long escalaId);

    boolean existsByFuncaoMusicalId(Long funcaoMusicalId);

    void deleteByMembroId(Long membroId);
}