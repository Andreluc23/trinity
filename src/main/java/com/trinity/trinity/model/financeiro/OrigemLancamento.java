package com.trinity.trinity.model.financeiro;

public enum OrigemLancamento {

    MANUAL("Manual"),
    PIX("PIX pelo sistema");

    private final String descricao;

    OrigemLancamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}