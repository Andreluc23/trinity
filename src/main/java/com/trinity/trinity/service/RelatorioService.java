package com.trinity.trinity.service;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Lancamento;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.Patrimonio;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.repository.LancamentoRepository;
import com.trinity.trinity.repository.LancamentoSpecifications;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.PatrimonioRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final Sort MAIS_RECENTES_PRIMEIRO =
            Sort.by(Sort.Direction.DESC, "data").and(Sort.by(Sort.Direction.DESC, "id"));

    private static final String ESTADO_BOM = "Bom";

    private final LancamentoRepository lancamentoRepository;
    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PatrimonioRepository patrimonioRepository;

    public RelatorioService(LancamentoRepository lancamentoRepository,
                            MembroRepository membroRepository,
                            UsuarioRepository usuarioRepository,
                            PatrimonioRepository patrimonioRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
        this.patrimonioRepository = patrimonioRepository;
    }

    // ---------- Financeiro ----------

    public RelatorioFinanceiro gerarFinanceiro(FiltroRelatorioFinanceiro filtro) {
        LocalDate inicio = filtro.inicio() != null ? filtro.inicio() : YearMonth.now().atDay(1);
        LocalDate fim = filtro.fim() != null ? filtro.fim() : YearMonth.now().atEndOfMonth();

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        List<Lancamento> lancamentos = lancamentoRepository.findAll(
                LancamentoSpecifications.filtrar(inicio, fim, filtro.tipo(),
                        filtro.categoriaId(), filtro.membroId(), filtro.texto()),
                MAIS_RECENTES_PRIMEIRO);

        return new RelatorioFinanceiro(inicio, fim, lancamentos,
                somar(lancamentos, TipoLancamento.ENTRADA),
                somar(lancamentos, TipoLancamento.SAIDA));
    }

    private BigDecimal somar(List<Lancamento> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == tipo)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---------- Membros ----------

    public RelatorioMembros gerarMembros(FiltroRelatorioMembros filtro) {
        List<Membro> todos = membroRepository.findAllByOrderByNomeCompletoAsc();

        Set<Long> membrosComAcesso = usuarioRepository.findAll().stream()
                .map(usuario -> usuario.getMembro().getId())
                .collect(Collectors.toSet());

        String nomeBuscado = filtro.nome() == null ? "" : filtro.nome().trim().toLowerCase();

        List<RelatorioMembros.Linha> linhas = todos.stream()
                .filter(membro -> filtro.ativo() == null || membro.isAtivo() == filtro.ativo())
                .filter(membro -> filtro.comAcesso() == null
                        || membrosComAcesso.contains(membro.getId()) == filtro.comAcesso())
                .filter(membro -> membro.getNomeCompleto().toLowerCase().contains(nomeBuscado))
                .map(membro -> new RelatorioMembros.Linha(membro, membrosComAcesso.contains(membro.getId())))
                .toList();

        long ativos = todos.stream().filter(Membro::isAtivo).count();

        return new RelatorioMembros(linhas, todos.size(), ativos, todos.size() - ativos, membrosComAcesso.size());
    }

    // ---------- Patrimônio ----------

    public RelatorioPatrimonio gerarPatrimonio(FiltroRelatorioPatrimonio filtro) {
        String nomeBuscado = filtro.nome() == null ? "" : filtro.nome().trim().toLowerCase();

        List<Patrimonio> itens = patrimonioRepository.findAll().stream()
                .filter(p -> filtro.ativo() == null || p.isAtivo() == filtro.ativo())
                .filter(p -> vazio(filtro.categoria()) || filtro.categoria().equalsIgnoreCase(p.getCategoria()))
                .filter(p -> vazio(filtro.estado()) || filtro.estado().equals(p.getEstadoConservacao()))
                .filter(p -> vazio(filtro.localizacao()) || filtro.localizacao().equalsIgnoreCase(p.getLocalizacao()))
                .filter(p -> p.getNome().toLowerCase().contains(nomeBuscado))
                .sorted(Comparator.comparing(Patrimonio::getCategoria, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Patrimonio::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        long totalUnidades = itens.stream().mapToLong(Patrimonio::getQuantidade).sum();
        long emBomEstado = itens.stream().filter(p -> ESTADO_BOM.equals(p.getEstadoConservacao())).count();

        return new RelatorioPatrimonio(itens, totalUnidades, emBomEstado, itens.size() - emBomEstado);
    }

    /**
     * Valores distintos de um campo, em ordem alfabética, para as opções dos filtros.
     */
    public List<String> opcoesDePatrimonio(Function<Patrimonio, String> campo) {
        return patrimonioRepository.findAll().stream()
                .map(campo)
                .filter(valor -> !vazio(valor))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}