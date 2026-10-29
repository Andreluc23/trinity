package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.CategoriaFinanceira;
import com.trinity.trinity.model.Contribuicao;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.StatusContribuicao;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.repository.ContribuicaoRepository;
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

    public void informarPagamento(Long id, String emailMembro) {
        Contribuicao contribuicao = buscarDoMembro(id, emailMembro);

        if (contribuicao.getStatus() != StatusContribuicao.AGUARDANDO_PAGAMENTO) {
            throw new RegraNegocioException("Esta contribuição não está aguardando pagamento.");
        }

        contribuicao.informarPagamento();
        contribuicaoRepository.save(contribuicao);
    }

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

    public List<Contribuicao> listarPendentes() {
        return contribuicaoRepository.findByStatusInOrderByCriadaEmAsc(PENDENTES);
    }

    public long contarAguardandoConfirmacao() {
        return contribuicaoRepository.countByStatus(StatusContribuicao.AGUARDANDO_CONFIRMACAO);
    }

    @Transactional
    public void confirmar(Long id) {
        Contribuicao contribuicao = buscarPendente(id);
        contribuicao.confirmar(lancamentoService.registrarEntradaPix(contribuicao));
        contribuicaoRepository.save(contribuicao);
    }

    public void rejeitar(Long id) {
        Contribuicao contribuicao = buscarPendente(id);
        contribuicao.rejeitar();
        contribuicaoRepository.save(contribuicao);
    }

    // ---------- Apoio ----------

    private Contribuicao buscarPorId(Long id) {
        return contribuicaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contribuição não encontrada"));
    }

    private Contribuicao buscarPendente(Long id) {
        Contribuicao contribuicao = buscarPorId(id);

        if (!contribuicao.getStatus().isPendente()) {
            throw new RegraNegocioException("Esta contribuição já foi " +
                    contribuicao.getStatus().getDescricao().toLowerCase() + ".");
        }

        return contribuicao;
    }
}