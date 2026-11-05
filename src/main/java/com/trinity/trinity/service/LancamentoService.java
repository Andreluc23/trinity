package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.*;
import com.trinity.trinity.repository.LancamentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaFinanceiraService categoriaService;
    private final MembroService membroService;

    public LancamentoService(LancamentoRepository lancamentoRepository,
                             CategoriaFinanceiraService categoriaService,
                             MembroService membroService) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaService = categoriaService;
        this.membroService = membroService;
    }

    public List<Lancamento> listarDoMes(YearMonth mes) {
        return lancamentoRepository.findByAtivoTrueAndDataBetweenOrderByDataDescIdDesc(
                mes.atDay(1), mes.atEndOfMonth());
    }

    public BigDecimal somarDoMes(TipoLancamento tipo, YearMonth mes) {
        return lancamentoRepository.somar(tipo, mes.atDay(1), mes.atEndOfMonth());
    }

    public Lancamento buscarPorId(Long id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }

    public Lancamento salvar(Lancamento lancamento) {
        validar(lancamento);

        CategoriaFinanceira categoria = categoriaService.buscarPorId(lancamento.getCategoria().getId());

        if (lancamento.getId() == null && !categoria.isAtivo()) {
            throw new RegraNegocioException("A categoria selecionada está inativa.");
        }

        manterDadosControladosPeloSistema(lancamento);

        lancamento.setCategoria(categoria);
        lancamento.setTipo(categoria.getTipo());
        lancamento.setMembro(resolverMembro(lancamento.getMembro()));
        lancamento.setDescricao(lancamento.getDescricao().trim());

        return lancamentoRepository.save(lancamento);
    }

    public void inativar(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamento.setAtivo(false);
        lancamentoRepository.save(lancamento);
    }

    /**
     * Origem e status nunca vêm do formulário: num lançamento novo são definidos aqui,
     * e numa edição são mantidos como estão no banco.
     */
    private void manterDadosControladosPeloSistema(Lancamento lancamento) {
        if (lancamento.getId() == null) {
            lancamento.setOrigem(OrigemLancamento.MANUAL);
            lancamento.setAtivo(true);
            return;
        }

        Lancamento existente = buscarPorId(lancamento.getId());
        lancamento.setOrigem(existente.getOrigem());
        lancamento.setAtivo(existente.isAtivo());
    }

    /**
     * O membro é opcional. Quando nenhum é selecionado, o formulário envia um id vazio.
     */
    private Membro resolverMembro(Membro membro) {
        if (membro == null || membro.getId() == null) {
            return null;
        }
        return membroService.buscarPorId(membro.getId());
    }

    private void validar(Lancamento lancamento) {
        if (lancamento.getDescricao() == null || lancamento.getDescricao().isBlank()) {
            throw new RegraNegocioException("Informe a descrição do lançamento.");
        }

        if (lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor deve ser maior que zero.");
        }

        if (lancamento.getData() == null) {
            throw new RegraNegocioException("Informe a data do lançamento.");
        }

        if (lancamento.getData().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data do lançamento não pode ser futura.");
        }

        if (lancamento.getCategoria() == null || lancamento.getCategoria().getId() == null) {
            throw new RegraNegocioException("Selecione a categoria.");
        }
    }
    public Lancamento registrarEntradaPix(Contribuicao contribuicao) {
        Lancamento lancamento = new Lancamento();
        lancamento.setDescricao("Contribuição PIX " + contribuicao.getIdentificador());
        lancamento.setValor(contribuicao.getValor());
        lancamento.setData(LocalDate.now());
        lancamento.setCategoria(contribuicao.getCategoria());
        lancamento.setTipo(TipoLancamento.ENTRADA);
        lancamento.setMembro(contribuicao.getMembro());
        lancamento.setOrigem(OrigemLancamento.PIX);

        return lancamentoRepository.save(lancamento);
    }
    public BigDecimal saldoGeral() {
        return lancamentoRepository.somarTudo(TipoLancamento.ENTRADA)
                .subtract(lancamentoRepository.somarTudo(TipoLancamento.SAIDA));
    }

    public List<ResumoMensal> resumoUltimosMeses(int quantidade) {
        YearMonth atual = YearMonth.now();
        List<ResumoMensal> resumo = new ArrayList<>();

        for (int i = quantidade - 1; i >= 0; i--) {
            YearMonth mes = atual.minusMonths(i);
            resumo.add(new ResumoMensal(mes,
                    somarDoMes(TipoLancamento.ENTRADA, mes),
                    somarDoMes(TipoLancamento.SAIDA, mes)));
        }

        return resumo;
    }
}