package com.trinity.trinity.service.relatorios;

import com.trinity.trinity.model.financeiro.Lancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioFinanceiro(
        LocalDate inicio,
        LocalDate fim,
        List<Lancamento> lancamentos,
        BigDecimal entradas,
        BigDecimal saidas) {

    /** Saldo do período: entradas menos saídas. */
    public BigDecimal saldo() {
        return entradas.subtract(saidas);
    }
}