package com.trinity.trinity.model.financeiro;

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