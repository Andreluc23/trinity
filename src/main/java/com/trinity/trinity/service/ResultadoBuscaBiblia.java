package com.trinity.trinity.service;

import java.util.List;

/**
 * Versículos exibidos (limitados) e o total encontrado.
 */
public record ResultadoBuscaBiblia(List<VersiculoBiblico> versiculos, int total) {

    public boolean isLimitado() {
        return total > versiculos.size();
    }
}