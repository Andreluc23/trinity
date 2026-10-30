package com.trinity.trinity.service;

import com.trinity.trinity.model.Membro;

import java.util.List;

public record RelatorioMembros(
        List<Linha> linhas,
        long total,
        long ativos,
        long inativos,
        long comAcesso) {

    public record Linha(Membro membro, boolean possuiAcesso) {
    }
}