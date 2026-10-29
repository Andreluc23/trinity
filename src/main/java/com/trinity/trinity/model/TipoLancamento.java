package com.trinity.trinity.model;

public enum TipoLancamento {

    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String descricao;

    TipoLancamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}