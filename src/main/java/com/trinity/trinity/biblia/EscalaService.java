package com.trinity.trinity.biblia;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Escala;
import com.trinity.trinity.model.EscalaItem;
import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.repository.EscalaItemRepository;
import com.trinity.trinity.repository.EscalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public EscalaService(EscalaRepository escalaRepository,
                         EscalaItemRepository escalaItemRepository,
                         MembroService membroService,
                         FuncaoMusicalService funcaoMusicalService) {
        this.escalaRepository = escalaRepository;
        this.escalaItemRepository = escalaItemRepository;
        this.membroService = membroService;
        this.funcaoMusicalService = funcaoMusicalService;
    }

    public List<Escala> listarTodas() {
        return escalaRepository.findAllByOrderByDataDescHorarioDesc();
    }

    public Escala buscarPorId(Long id) {
        return escalaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escala não encontrada"));
    }

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

    public void desativar(Long id) {
        alterarStatus(id, false);
    }

    public void ativar(Long id) {
        alterarStatus(id, true);
    }

    private void alterarStatus(Long id, boolean ativo) {
        Escala escala = buscarPorId(id);
        escala.setAtivo(ativo);
        escalaRepository.save(escala);
    }

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