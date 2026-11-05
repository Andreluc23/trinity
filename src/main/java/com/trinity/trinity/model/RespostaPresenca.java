package com.trinity.trinity.model;

public enum RespostaPresenca {

    VOU("Vou"),
    NAO_VOU("Não vou");

    private final String descricao;

    RespostaPresenca(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}