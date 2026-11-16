package com.trinity.trinity.service.relatorios;

import com.trinity.trinity.model.patrimonio.Patrimonio;

import java.util.List;

public record RelatorioPatrimonio(
        List<Patrimonio> itens,
        long totalUnidades,
        long emBomEstado,
        long precisamAtencao) {
}