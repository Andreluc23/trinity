package com.trinity.trinity.service;

import com.trinity.trinity.model.Patrimonio;
import com.trinity.trinity.repository.PatrimonioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatrimonioService {

    private final PatrimonioRepository patrimonioRepository;

    public PatrimonioService(PatrimonioRepository patrimonioRepository) {
        this.patrimonioRepository = patrimonioRepository;
    }

    public List<Patrimonio> listarTodos() {
        return patrimonioRepository.findAll();
    }
    public List<Patrimonio> buscar(String termo) {

        if (termo == null || termo.isBlank()) {
            return patrimonioRepository.findAll();
        }

        return patrimonioRepository
                .findByNomeContainingIgnoreCaseOrCategoriaContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(
                        termo,
                        termo,
                        termo
                );
    }

    public Patrimonio buscarPorId(Long id) {
        return patrimonioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patrimônio não encontrado"));
    }

    public Patrimonio salvar(Patrimonio patrimonio) {
        return patrimonioRepository.save(patrimonio);
    }

    public void desativar(Long id) {
        Patrimonio patrimonio = buscarPorId(id);
        patrimonio.setAtivo(false);
        patrimonioRepository.save(patrimonio);
    }

    public void ativar(Long id) {
        Patrimonio patrimonio = buscarPorId(id);
        patrimonio.setAtivo(true);
        patrimonioRepository.save(patrimonio);
    }

}