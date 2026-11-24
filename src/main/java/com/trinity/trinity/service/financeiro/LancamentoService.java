package com.trinity.trinity.service.financeiro;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.financeiro.*;
import com.trinity.trinity.model.membros.Membro;
import com.trinity.trinity.repository.financeiro.LancamentoRepository;
import com.trinity.trinity.service.membros.MembroService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /** Lançamentos ativos de um mês, do mais recente para o mais antigo. */
    public List<Lancamento> listarDoMes(YearMonth mes) {
        return lancamentoRepository.findByAtivoTrueAndDataBetweenOrderByDataDescIdDesc(
                mes.atDay(1), mes.atEndOfMonth());
    }

    /** Soma as entradas ou as saídas ativas de um mês. */
    public BigDecimal somarDoMes(TipoLancamento tipo, YearMonth mes) {
        return lancamentoRepository.somar(tipo, mes.atDay(1), mes.atEndOfMonth());
    }

    /** Busca um lançamento; se não existir, lança erro de recurso não encontrado. */
    public Lancamento buscarPorId(Long id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }

    /** Valida e grava o lançamento; o tipo vem da categoria e a origem é definida pelo sistema. */
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

    /** Inativa o lançamento, mantendo o registro mas tirando-o dos totais. */
    public void inativar(Long id) {
        Lancamento lancamento = buscarPorId(id);
        lancamento.setAtivo(false);
        lancamentoRepository.save(lancamento);
    }
    /** Exclui o lançamento manual; os de PIX são recusados. */
    public void excluir(Long id) {
        Lancamento lancamento = buscarPorId(id);

        if (lancamento.getOrigem() == OrigemLancamento.PIX) {
            throw new RegraNegocioException(
                    "Lançamentos de PIX estão ligados a uma contribuição e não podem ser excluídos. Use Inativar.");
        }

        lancamentoRepository.delete(lancamento);
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

    /** Confere descrição, valor positivo, data (não futura) e categoria. */
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
    /** Cria o lançamento de entrada correspondente a uma contribuição PIX confirmada. */
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

    /** Saldo de todo o histórico (entradas menos saídas), arredondado em centavos. */
    public BigDecimal saldoGeral() {
        return emCentavos(lancamentoRepository.somarTudo(TipoLancamento.ENTRADA)
                .subtract(lancamentoRepository.somarTudo(TipoLancamento.SAIDA)));
    }

    /** Entradas e saídas de cada um dos últimos meses, para o gráfico do dashboard. */
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
    /**
     * O SQLite guarda decimais como ponto flutuante: arredonda para centavos.
     */
    private static BigDecimal emCentavos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}