package com.trinity.trinity.service.relatorios;

public record FiltroRelatorioPatrimonio(
        Boolean ativo,
        String categoria,
        String estado,
        String localizacao,
        String nome) {
}