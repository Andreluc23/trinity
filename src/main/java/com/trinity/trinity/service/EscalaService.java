package com.trinity.trinity.service;

import com.trinity.trinity.model.Escala;
import com.trinity.trinity.repository.EscalaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalaService {

    private final EscalaRepository escalaRepository;

    public EscalaService(EscalaRepository escalaRepository) {
        this.escalaRepository = escalaRepository;
    }

    public List<Escala> listarTodas() {
        return escalaRepository.findAll();
    }

    public Escala buscarPorId(Long id) {
        return escalaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escala não encontrada"));
    }

    public Escala salvar(Escala escala) {
        return escalaRepository.save(escala);
    }

    public void desativar(Long id) {
        Escala escala = buscarPorId(id);
        escala.setAtivo(false);
        escalaRepository.save(escala);
    }

    public void ativar(Long id) {
        Escala escala = buscarPorId(id);
        escala.setAtivo(true);
        escalaRepository.save(escala);
    }
}