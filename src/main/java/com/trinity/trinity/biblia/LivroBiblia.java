package com.trinity.trinity.biblia;

import java.util.List;

/**
 * Um livro da Bíblia. Cada item de "capitulos" é a lista de versículos daquele capítulo.
 */
public record LivroBiblia(int numero, String nome, String testamento, List<List<String>> capitulos) {

    public int quantidadeCapitulos() {
        return capitulos.size();
    }
}