package com.trinity.trinity.service;

import com.trinity.trinity.model.Lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioFinanceiro(
        LocalDate inicio,
        LocalDate fim,
        List<Lancamento> lancamentos,
        BigDecimal entradas,
        BigDecimal saidas) {

    public BigDecimal saldo() {
        return entradas.subtract(saidas);
    }
}