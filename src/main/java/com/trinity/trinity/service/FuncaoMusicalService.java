package com.trinity.trinity.service;

import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.repository.FuncaoMusicalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuncaoMusicalService {

    private final FuncaoMusicalRepository funcaoMusicalRepository;

    public FuncaoMusicalService(FuncaoMusicalRepository funcaoMusicalRepository) {
        this.funcaoMusicalRepository = funcaoMusicalRepository;
    }

    public List<FuncaoMusical> listarTodas() {
        return funcaoMusicalRepository.findAll();
    }

    public FuncaoMusical buscarPorId(Long id) {
        return funcaoMusicalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Função musical não encontrada"));
    }

    public FuncaoMusical salvar(FuncaoMusical funcaoMusical) {

        if (funcaoMusical.getId() == null
                && funcaoMusicalRepository.existsByNomeIgnoreCase(funcaoMusical.getNome())) {
            throw new RuntimeException("Essa função musical já está cadastrada.");
        }

        return funcaoMusicalRepository.save(funcaoMusical);
    }

    public void desativar(Long id) {
        FuncaoMusical funcao = buscarPorId(id);
        funcao.setAtivo(false);
        funcaoMusicalRepository.save(funcao);
    }

    public void ativar(Long id) {
        FuncaoMusical funcao = buscarPorId(id);
        funcao.setAtivo(true);
        funcaoMusicalRepository.save(funcao);
    }
}