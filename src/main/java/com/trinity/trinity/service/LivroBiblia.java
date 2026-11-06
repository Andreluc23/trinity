package com.trinity.trinity.service;

import java.util.List;

/**
 * Um livro da Bíblia. Cada item de "capitulos" é a lista de versículos daquele capítulo.
 */
public record LivroBiblia(int numero, String nome, String testamento, List<List<String>> capitulos) {

    public int quantidadeCapitulos() {
        return capitulos.size();
    }

    /**
     * Divisão tradicional dos livros, usada para agrupá-los na tela.
     */
    public String categoria() {
        if (numero <= 5) return "Pentateuco";
        if (numero <= 17) return "Livros Históricos";
        if (numero <= 22) return "Livros Poéticos";
        if (numero <= 27) return "Profetas Maiores";
        if (numero <= 39) return "Profetas Menores";
        if (numero <= 43) return "Evangelhos";
        if (numero == 44) return "Livro Histórico";
        if (numero <= 57) return "Cartas de Paulo";
        if (numero <= 65) return "Cartas Gerais";
        return "Profecia";
    }
}