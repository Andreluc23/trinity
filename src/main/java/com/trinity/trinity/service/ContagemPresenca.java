package com.trinity.trinity.service;

import com.trinity.trinity.model.RespostaPresenca;

public record ContagemPresenca(long vao, long naoVao) {

    public static ContagemPresenca de(RespostaPresenca resposta) {
        return resposta == RespostaPresenca.VOU
                ? new ContagemPresenca(1, 0)
                : new ContagemPresenca(0, 1);
    }

    public ContagemPresenca somar(ContagemPresenca outra) {
        return new ContagemPresenca(vao + outra.vao, naoVao + outra.naoVao);
    }
}