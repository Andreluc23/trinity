package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Texto bíblico: Bíblia Livre (BLIVRE), licença CC BY 4.0.
 * Carregado uma única vez, na inicialização, a partir de um arquivo da própria aplicação.
 */
@Service
public class BibliaService {

    private static final String ARQUIVO = "biblia/biblia-livre.tsv";
    private static final int TAMANHO_MINIMO_BUSCA = 3;
    private static final int LIMITE_RESULTADOS = 100;

    private final List<LivroBiblia> livros;
    private final List<VersiculoIndexado> indice;

    /**
     * Versículo acompanhado do texto já normalizado (sem acentos, minúsculo), para a busca.
     */
    private record VersiculoIndexado(VersiculoBiblico versiculo, String textoNormalizado) {
    }

    public BibliaService() {
        this.livros = carregar();
        this.indice = indexar(livros);
    }

    // ---------- Consulta ----------

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

    // ---------- Navegação entre capítulos ----------

    public ReferenciaBiblica capituloAnterior(int numeroLivro, int capitulo) {
        if (capitulo > 1) {
            return new ReferenciaBiblica(buscarLivro(numeroLivro), capitulo - 1);
        }
        if (numeroLivro > 1) {
            LivroBiblia anterior = buscarLivro(numeroLivro - 1);
            return new ReferenciaBiblica(anterior, anterior.quantidadeCapitulos());
        }
        return null;
    }

    public ReferenciaBiblica proximoCapitulo(int numeroLivro, int capitulo) {
        LivroBiblia livro = buscarLivro(numeroLivro);

        if (capitulo < livro.quantidadeCapitulos()) {
            return new ReferenciaBiblica(livro, capitulo + 1);
        }
        if (numeroLivro < livros.size()) {
            return new ReferenciaBiblica(buscarLivro(numeroLivro + 1), 1);
        }
        return null;
    }

    // ---------- Busca ----------

    public ResultadoBuscaBiblia buscar(String termo) {
        String procurado = normalizar(termo == null ? "" : termo.trim());

        if (procurado.length() < TAMANHO_MINIMO_BUSCA) {
            throw new RegraNegocioException(
                    "Digite pelo menos " + TAMANHO_MINIMO_BUSCA + " letras para buscar.");
        }

        List<VersiculoBiblico> encontrados = indice.stream()
                .filter(item -> item.textoNormalizado().contains(procurado))
                .map(VersiculoIndexado::versiculo)
                .toList();

        return new ResultadoBuscaBiblia(
                encontrados.stream().limit(LIMITE_RESULTADOS).toList(),
                encontrados.size());
    }

    // ---------- Carregamento ----------

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

    private List<VersiculoIndexado> indexar(List<LivroBiblia> livros) {
        List<VersiculoIndexado> itens = new ArrayList<>();

        for (LivroBiblia livro : livros) {
            for (int c = 0; c < livro.quantidadeCapitulos(); c++) {
                List<String> versiculos = livro.capitulos().get(c);

                for (int v = 0; v < versiculos.size(); v++) {
                    String texto = versiculos.get(v);
                    itens.add(new VersiculoIndexado(
                            new VersiculoBiblico(livro, c + 1, v + 1, texto),
                            normalizar(texto)));
                }
            }
        }

        return List.copyOf(itens);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}