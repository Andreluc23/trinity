package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.model.Aviso;
import com.trinity.trinity.repository.AvisoRepository;
import com.trinity.trinity.repository.LeituraAvisoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final LeituraAvisoRepository leituraRepository;

    public AvisoService(AvisoRepository avisoRepository, LeituraAvisoRepository leituraRepository) {
        this.avisoRepository = avisoRepository;
        this.leituraRepository = leituraRepository;
    }

    public List<Aviso> listarTodos() {
        return avisoRepository.findAllByOrderByDataPublicacaoDesc();
    }

    public List<Aviso> listarAtivos() {
        return avisoRepository.findByAtivoTrueOrderByDataPublicacaoDesc();
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