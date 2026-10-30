package com.trinity.trinity.repository;

import com.trinity.trinity.model.Lancamento;
import com.trinity.trinity.model.TipoLancamento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LancamentoSpecifications {

    private LancamentoSpecifications() {
    }

    public static Specification<Lancamento> filtrar(LocalDate inicio,
                                                    LocalDate fim,
                                                    TipoLancamento tipo,
                                                    Long categoriaId,
                                                    Long membroId,
                                                    String texto) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            condicoes.add(cb.isTrue(root.get("ativo")));
            condicoes.add(cb.between(root.get("data"), inicio, fim));

            if (tipo != null) {
                condicoes.add(cb.equal(root.get("tipo"), tipo));
            }

            if (categoriaId != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }

            if (membroId != null) {
                condicoes.add(cb.equal(root.get("membro").get("id"), membroId));
            }

            if (texto != null && !texto.isBlank()) {
                condicoes.add(cb.like(cb.lower(root.get("descricao")),
                        "%" + texto.trim().toLowerCase() + "%"));
            }

            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }
}