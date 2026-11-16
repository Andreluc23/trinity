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

    public Page<Aviso> listarTodos(int pagina) {
        return avisoRepository.findAllByOrderByDataPublicacaoDesc(PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    public Page<Aviso> listarAtivos(int pagina) {
        return avisoRepository.findByAtivoTrueOrderByDataPublicacaoDesc(PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    public List<Aviso> listarRecentes() {
        return avisoRepository.findTop5ByAtivoTrueOrderByDataPublicacaoDesc();
    }

    public Aviso salvar(Aviso aviso) {
        return avisoRepository.save(aviso);
    }

    public Aviso buscarPorId(Long id) {
        return avisoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aviso não encontrado"));
    }

    public void desativar(Long id) {
        Aviso aviso = buscarPorId(id);
        aviso.setAtivo(false);
        avisoRepository.save(aviso);
    }

    public void ativar(Long id) {
        Aviso aviso = buscarPorId(id);
        aviso.setAtivo(true);
        avisoRepository.save(aviso);
    }
    @Transactional
    public void excluir(Long id) {
        Aviso aviso = buscarPorId(id);
        leituraRepository.deleteByAvisoId(id);
        avisoRepository.delete(aviso);
    }
}