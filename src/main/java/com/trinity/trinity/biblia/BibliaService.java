package com.trinity.trinity.biblia;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Texto bíblico: Bíblia Livre (BLIVRE), licença CC BY 4.0.
 * Carregado uma única vez, na inicialização, a partir de um arquivo da própria aplicação.
 */
@Service
public class BibliaService {

    private static final String ARQUIVO = "biblia/biblia-livre.tsv";

    private final List<LivroBiblia> livros;

    public BibliaService() {
        this.livros = carregar();
    }

    public List<LivroBiblia> listarLivros() {
        return livros;
    }

    public LivroBiblia buscarLivro(int numero) {
        if (numero < 1 || numero > livros.size()) {
            throw new RecursoNaoEncontradoException("Livro não encontrado");
        }
        return livros.get(numero - 1);
    }

    public List<String> buscarCapitulo(int numeroLivro, int capitulo) {
        LivroBiblia livro = buscarLivro(numeroLivro);

        if (capitulo < 1 || capitulo > livro.quantidadeCapitulos()) {
            throw new RecursoNaoEncontradoException("Capítulo não encontrado");
        }
        return livro.capitulos().get(capitulo - 1);
    }

    private List<LivroBiblia> carregar() {
        Map<Integer, LivroBiblia> livrosLidos = new LinkedHashMap<>();

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
                new ClassPathResource(ARQUIVO).getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }

                String[] campos = linha.split("\t", 6);
                int numero = Integer.parseInt(campos[0]);
                int capitulo = Integer.parseInt(campos[3]);

                LivroBiblia livro = livrosLidos.computeIfAbsent(numero,
                        n -> new LivroBiblia(n, campos[1], campos[2], new ArrayList<>()));

                while (livro.capitulos().size() < capitulo) {
                    livro.capitulos().add(new ArrayList<>());
                }
                livro.capitulos().get(capitulo - 1).add(campos[5]);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível carregar o texto da Bíblia", e);
        }

        return List.copyOf(livrosLidos.values());
    }
}