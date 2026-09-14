package com.trinity.trinity.service;

import com.trinity.trinity.model.Aviso;
import com.trinity.trinity.repository.AvisoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;

    public AvisoService(AvisoRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    public List<Aviso> listarTodos() {
        return avisoRepository.findAll();
    }

    public Aviso salvar(Aviso aviso) {
        return avisoRepository.save(aviso);
    }

    public Aviso buscarPorId(Long id) {
        return avisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aviso não encontrado"));
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
    public List<Aviso> listarRecentes() {
        return avisoRepository.findTop5ByAtivoTrueOrderByDataPublicacaoDesc();
    }
}