package com.trinity.trinity.service;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Lancamento;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.repository.LancamentoRepository;
import com.trinity.trinity.repository.LancamentoSpecifications;
import com.trinity.trinity.repository.MembroRepository;
import com.trinity.trinity.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final Sort MAIS_RECENTES_PRIMEIRO =
            Sort.by(Sort.Direction.DESC, "data").and(Sort.by(Sort.Direction.DESC, "id"));

    private final LancamentoRepository lancamentoRepository;
    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;

    public RelatorioService(LancamentoRepository lancamentoRepository,
                            MembroRepository membroRepository,
                            UsuarioRepository usuarioRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.membroRepository = membroRepository;
        this.usuarioRepository = usuarioRepository;
    }

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
}