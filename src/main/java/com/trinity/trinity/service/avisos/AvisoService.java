package com.trinity.trinity.service.avisos;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.model.avisos.Aviso;
import com.trinity.trinity.repository.avisos.AvisoRepository;
import com.trinity.trinity.repository.avisos.LeituraAvisoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final LeituraAvisoRepository leituraRepository;
    private static final int ITENS_POR_PAGINA = 12;

    public AvisoService(AvisoRepository avisoRepository, LeituraAvisoRepository leituraRepository) {
        this.avisoRepository = avisoRepository;
        this.leituraRepository = leituraRepository;
    }

    /** Todos os avisos, do mais recente para o mais antigo, em páginas de 12. */
    public Page<Aviso> listarTodos(int pagina) {
        return avisoRepository.findAllByOrderByDataPublicacaoDesc(PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    /** Só os avisos ativos, do mais recente para o mais antigo, em páginas de 12. */
    public Page<Aviso> listarAtivos(int pagina) {
        return avisoRepository.findByAtivoTrueOrderByDataPublicacaoDesc(PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    /** Os 5 avisos ativos mais recentes, para o dashboard. */
    public List<Aviso> listarRecentes() {
        return avisoRepository.findTop5ByAtivoTrueOrderByDataPublicacaoDesc();
    }

    /** Grava um aviso novo ou editado. */
    public Aviso salvar(Aviso aviso) {
        return avisoRepository.save(aviso);
    }

    /** Busca um aviso; se não existir, lança erro de recurso não encontrado. */
    public Aviso buscarPorId(Long id) {
        return avisoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aviso não encontrado"));
    }

    /** Marca o aviso como inativo. */
    public void desativar(Long id) {
        Aviso aviso = buscarPorId(id);
        aviso.setAtivo(false);
        avisoRepository.save(aviso);
    }

    /** Marca o aviso como ativo. */
    public void ativar(Long id) {
        Aviso aviso = buscarPorId(id);
        aviso.setAtivo(true);
        avisoRepository.save(aviso);
    }
    /** Exclui o aviso e as leituras dele, tudo na mesma transação. */
    @Transactional
    public void excluir(Long id) {
        Aviso aviso = buscarPorId(id);
        leituraRepository.deleteByAvisoId(id);
        avisoRepository.delete(aviso);
    }
}