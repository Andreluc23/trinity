package com.trinity.trinity.model.financeiro;

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

    /** Informa se a contribuição ainda aguarda alguma ação (pagamento ou confirmação). */
    public boolean isPendente() {
        return this == AGUARDANDO_PAGAMENTO || this == AGUARDANDO_CONFIRMACAO;
    }
}