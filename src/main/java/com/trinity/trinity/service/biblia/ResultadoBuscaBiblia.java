package com.trinity.trinity.service.biblia;

import java.util.List;

/**
 * Versículos exibidos (limitados) e o total encontrado.
 */
public record ResultadoBuscaBiblia(List<VersiculoBiblico> versiculos, int total) {

    /** Informa se há mais versículos encontrados do que os exibidos. */
    public boolean isLimitado() {
        return total > versiculos.size();
    }
}