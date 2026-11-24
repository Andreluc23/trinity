package com.trinity.trinity.service.escalas;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.escalas.Escala;
import com.trinity.trinity.model.escalas.EscalaItem;
import com.trinity.trinity.model.escalas.FuncaoMusical;
import com.trinity.trinity.model.membros.Membro;
import com.trinity.trinity.repository.escalas.EscalaItemRepository;
import com.trinity.trinity.repository.escalas.EscalaRepository;

import com.trinity.trinity.service.membros.MembroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EscalaService {

    private final EscalaRepository escalaRepository;
    private final EscalaItemRepository escalaItemRepository;
    private final MembroService membroService;
    private final FuncaoMusicalService funcaoMusicalService;
    private static final int ITENS_POR_PAGINA = 12;

    public EscalaService(EscalaRepository escalaRepository,
                         EscalaItemRepository escalaItemRepository,
                         MembroService membroService,
                         FuncaoMusicalService funcaoMusicalService) {
        this.escalaRepository = escalaRepository;
        this.escalaItemRepository = escalaItemRepository;
        this.membroService = membroService;
        this.funcaoMusicalService = funcaoMusicalService;
    }

    /**
     * Próximas escalas primeiro (da mais próxima para a mais distante),
     * depois as que já passaram (da mais recente para a mais antiga).
     */


    /** Escalas em páginas de 12: as próximas primeiro (da mais perto) e depois as passadas (da mais recente). */
    public Page<Escala> listarTodas(int pagina) {
        return escalaRepository.listarProximasPrimeiro(LocalDate.now(), PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    /** Busca uma escala; se não existir, lança erro de recurso não encontrado. */
    public Escala buscarPorId(Long id) {
        return escalaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escala não encontrada"));
    }

    /** Integrantes de uma escala, em ordem alfabética de função. */
    public List<EscalaItem> listarIntegrantes(Long escalaId) {
        return escalaItemRepository.findByEscalaIdOrderByFuncaoMusicalNomeAsc(escalaId);
    }

    /**
     * Grava a escala e substitui todos os seus integrantes pelos informados.
     * As validações acontecem antes de qualquer gravação.
     */
    @Transactional
    public Escala salvar(Escala escala, List<IntegranteEscala> integrantes) {
        validarDados(escala);
        List<EscalaItem> itens = montarItens(integrantes);

        Escala escalaSalva = escalaRepository.save(escala);

        escalaItemRepository.deleteByEscalaId(escalaSalva.getId());
        itens.forEach(item -> item.setEscala(escalaSalva));
        escalaItemRepository.saveAll(itens);

        return escalaSalva;
    }

    /** Marca a escala como inativa. */
    public void desativar(Long id) {
        alterarStatus(id, false);
    }

    /** Marca a escala como ativa. */
    public void ativar(Long id) {
        alterarStatus(id, true);
    }

    /** Exclui a escala e os seus integrantes, na mesma transação. */
    @Transactional
    public void excluir(Long id) {
        Escala escala = buscarPorId(id);
        escalaItemRepository.deleteByEscalaId(id);
        escalaRepository.delete(escala);
    }

    /** Troca o status ativo/inativo da escala. */
    private void alterarStatus(Long id, boolean ativo) {
        Escala escala = buscarPorId(id);
        escala.setAtivo(ativo);
        escalaRepository.save(escala);
    }

    /** Confere título, data e horário da escala e remove espaços do título. */
    private void validarDados(Escala escala) {
        if (escala.getTitulo() == null || escala.getTitulo().isBlank()) {
            throw new RegraNegocioException("Informe o título da escala.");
        }
        if (escala.getData() == null) {
            throw new RegraNegocioException("Informe a data da escala.");
        }
        if (escala.getHorario() == null) {
            throw new RegraNegocioException("Informe o horário da escala.");
        }
        escala.setTitulo(escala.getTitulo().trim());
    }

    /** Valida os integrantes (sem repetir membro nem função, ambos ativos) e monta os itens da escala. */
    private List<EscalaItem> montarItens(List<IntegranteEscala> integrantes) {
        Set<Long> membrosUsados = new HashSet<>();
        Set<Long> funcoesUsadas = new HashSet<>();
        List<EscalaItem> itens = new ArrayList<>();

        for (IntegranteEscala integrante : integrantes) {
            if (integrante.membroId() == null || integrante.funcaoId() == null) {
                throw new RegraNegocioException("Selecione o membro e a função em todas as linhas.");
            }

            Membro membro = membroService.buscarPorId(integrante.membroId());
            FuncaoMusical funcao = funcaoMusicalService.buscarPorId(integrante.funcaoId());

            if (!membrosUsados.add(membro.getId())) {
                throw new RegraNegocioException(membro.getNomeCompleto() + " aparece mais de uma vez na escala.");
            }
            if (!funcoesUsadas.add(funcao.getId())) {
                throw new RegraNegocioException("A função " + funcao.getNome() + " foi usada mais de uma vez.");
            }
            if (!membro.isAtivo()) {
                throw new RegraNegocioException(membro.getNomeCompleto() + " está inativo e não pode ser escalado.");
            }
            if (!funcao.isAtivo()) {
                throw new RegraNegocioException("A função " + funcao.getNome() + " está inativa.");
            }

            EscalaItem item = new EscalaItem();
            item.setMembro(membro);
            item.setFuncaoMusical(funcao);
            itens.add(item);
        }

        return itens;
    }
}