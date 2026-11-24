package com.trinity.trinity.service.financeiro;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.financeiro.CategoriaFinanceira;
import com.trinity.trinity.model.financeiro.Contribuicao;
import com.trinity.trinity.model.financeiro.StatusContribuicao;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.model.membros.Membro;
import com.trinity.trinity.repository.financeiro.ContribuicaoRepository;
import com.trinity.trinity.service.usuarios.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContribuicaoService {

    private static final List<StatusContribuicao> PENDENTES = List.of(
            StatusContribuicao.AGUARDANDO_PAGAMENTO,
            StatusContribuicao.AGUARDANDO_CONFIRMACAO);

    private final ContribuicaoRepository contribuicaoRepository;
    private final CategoriaFinanceiraService categoriaService;
    private final LancamentoService lancamentoService;
    private final UsuarioService usuarioService;

    public ContribuicaoService(ContribuicaoRepository contribuicaoRepository,
                               CategoriaFinanceiraService categoriaService,
                               LancamentoService lancamentoService,
                               UsuarioService usuarioService) {
        this.contribuicaoRepository = contribuicaoRepository;
        this.categoriaService = categoriaService;
        this.lancamentoService = lancamentoService;
        this.usuarioService = usuarioService;
    }

    // ---------- Ações do membro ----------

    /** Cria uma contribuição do membro (valor positivo e categoria de entrada ativa), aguardando pagamento. */
    public Contribuicao gerar(String emailMembro, BigDecimal valor, Long categoriaId) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor deve ser maior que zero.");
        }

        if (categoriaId == null) {
            throw new RegraNegocioException("Selecione o tipo de contribuição.");
        }

        CategoriaFinanceira categoria = categoriaService.buscarPorId(categoriaId);

        if (!categoria.isAtivo() || categoria.getTipo() != TipoLancamento.ENTRADA) {
            throw new RegraNegocioException("Tipo de contribuição inválido.");
        }

        Membro membro = usuarioService.buscarPorEmail(emailMembro).getMembro();

        return contribuicaoRepository.save(new Contribuicao(membro, categoria, valor));
    }

    /** O membro avisa que pagou; só vale para a contribuição dele que está aguardando pagamento. */
    public void informarPagamento(Long id, String emailMembro) {
        Contribuicao contribuicao = buscarDoMembro(id, emailMembro);

        if (contribuicao.getStatus() != StatusContribuicao.AGUARDANDO_PAGAMENTO) {
            throw new RegraNegocioException("Esta contribuição não está aguardando pagamento.");
        }

        contribuicao.informarPagamento();
        contribuicaoRepository.save(contribuicao);
    }

    /** Contribuições do membro, da mais recente para a mais antiga. */
    public List<Contribuicao> listarDoMembro(String emailMembro) {
        Long membroId = usuarioService.buscarPorEmail(emailMembro).getMembro().getId();
        return contribuicaoRepository.findByMembroIdOrderByCriadaEmDesc(membroId);
    }

    /**
     * Busca uma contribuição garantindo que ela pertence ao membro logado.
     * Assim, um membro não consegue ver nem alterar a contribuição de outro trocando o id na URL.
     */
    public Contribuicao buscarDoMembro(Long id, String emailMembro) {
        Contribuicao contribuicao = buscarPorId(id);
        String emailDono = contribuicao.getMembro().getEmail();

        if (emailDono == null || !emailDono.equalsIgnoreCase(emailMembro)) {
            throw new RecursoNaoEncontradoException("Contribuição não encontrada");
        }

        return contribuicao;
    }

    // ---------- Ações do admin ----------

    /** Contribuições aguardando pagamento ou confirmação, da mais antiga para a mais recente. */
    public List<Contribuicao> listarPendentes() {
        return contribuicaoRepository.findByStatusInOrderByCriadaEmAsc(PENDENTES);
    }

    /** Quantidade de contribuições que a tesouraria precisa confirmar. */
    public long contarAguardandoConfirmacao() {
        return contribuicaoRepository.countByStatus(StatusContribuicao.AGUARDANDO_CONFIRMACAO);
    }

    /** Confirma a contribuição e cria o lançamento de entrada PIX, tudo na mesma transação. */
    @Transactional
    public void confirmar(Long id) {
        Contribuicao contribuicao = buscarPendente(id);
        contribuicao.confirmar(lancamentoService.registrarEntradaPix(contribuicao));
        contribuicaoRepository.save(contribuicao);
    }

    /** Rejeita uma contribuição pendente. */
    public void rejeitar(Long id) {
        Contribuicao contribuicao = buscarPendente(id);
        contribuicao.rejeitar();
        contribuicaoRepository.save(contribuicao);
    }

    // ---------- Apoio ----------

    /** Busca uma contribuição; se não existir, lança erro de recurso não encontrado. */
    private Contribuicao buscarPorId(Long id) {
        return contribuicaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contribuição não encontrada"));
    }

    /** Busca uma contribuição e garante que ainda está pendente. */
    private Contribuicao buscarPendente(Long id) {
        Contribuicao contribuicao = buscarPorId(id);

        if (!contribuicao.getStatus().isPendente()) {
            throw new RegraNegocioException("Esta contribuição já foi " +
                    contribuicao.getStatus().getDescricao().toLowerCase() + ".");
        }

        return contribuicao;
    }
}