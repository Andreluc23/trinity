package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibliaServiceTest {

    private final BibliaService biblia = new BibliaService();

    @Test
    void deveCarregarOs66LivrosEOs31102Versiculos() {
        assertEquals(66, biblia.listarLivros().size());

        int versiculos = biblia.listarLivros().stream()
                .flatMap(livro -> livro.capitulos().stream())
                .mapToInt(capitulo -> capitulo.size())
                .sum();

        assertEquals(31102, versiculos);
    }

    @Test
    void deveEncontrarJoao3Versiculo16() {
        String versiculo = biblia.buscarCapitulo(43, 3).get(15);

        assertTrue(versiculo.startsWith("Porque Deus amou ao mundo"));
    }

    @Test
    void deveRecusarReferenciaInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> biblia.buscarCapitulo(1, 51));
        assertThrows(RecursoNaoEncontradoException.class, () -> biblia.buscarLivro(67));
    }
}