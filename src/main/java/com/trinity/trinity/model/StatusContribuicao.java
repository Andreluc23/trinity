package com.trinity.trinity.model;

public enum StatusContribuicao {

    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    AGUARDANDO_CONFIRMACAO("Aguardando confirmação"),
    CONFIRMADA("Confirmada"),
    REJEITADA("Rejeitada");

    private final String descricao;

    StatusContribuicao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPendente() {
        return this == AGUARDANDO_PAGAMENTO || this == AGUARDANDO_CONFIRMACAO;
    }
}