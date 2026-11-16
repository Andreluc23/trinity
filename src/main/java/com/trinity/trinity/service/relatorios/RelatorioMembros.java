package com.trinity.trinity.service.relatorios;

import com.trinity.trinity.model.membros.Membro;

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