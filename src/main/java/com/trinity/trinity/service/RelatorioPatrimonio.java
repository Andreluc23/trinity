package com.trinity.trinity.service;

import com.trinity.trinity.model.Patrimonio;

import java.util.List;

public record RelatorioPatrimonio(
        List<Patrimonio> itens,
        long totalUnidades,
        long emBomEstado,
        long precisamAtencao) {
}