package com.trinity.trinity.biblia;

import com.trinity.trinity.model.TipoLancamento;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record FiltroRelatorioFinanceiro(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
        TipoLancamento tipo,
        Long categoriaId,
        Long membroId,
        String texto) {
}