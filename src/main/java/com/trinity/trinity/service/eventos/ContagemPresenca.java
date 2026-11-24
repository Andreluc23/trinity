package com.trinity.trinity.service.eventos;


import com.trinity.trinity.model.eventos.RespostaPresenca;

public record ContagemPresenca(long vao, long naoVao) {

    /** Cria a contagem de uma única resposta: um "vou" ou um "não vou". */
    public static ContagemPresenca de(RespostaPresenca resposta) {
        return resposta == RespostaPresenca.VOU
                ? new ContagemPresenca(1, 0)
                : new ContagemPresenca(0, 1);
    }

    /** Soma duas contagens, devolvendo uma nova. */
    public ContagemPresenca somar(ContagemPresenca outra) {
        return new ContagemPresenca(vao + outra.vao, naoVao + outra.naoVao);
    }
}