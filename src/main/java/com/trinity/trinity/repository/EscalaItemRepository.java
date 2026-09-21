package com.trinity.trinity.repository;

import com.trinity.trinity.model.EscalaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaItemRepository extends JpaRepository<EscalaItem, Long> {

    List<EscalaItem> findByEscalaId(Long escalaId);
}