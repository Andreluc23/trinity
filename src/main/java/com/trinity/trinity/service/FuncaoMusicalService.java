package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
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
        return funcaoMusicalRepository.findAllByOrderByNomeAsc();
    }

    public List<FuncaoMusical> listarAtivas() {
        return funcaoMusicalRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public FuncaoMusical buscarPorId(Long id) {
        return funcaoMusicalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Função musical não encontrada"));
    }

    public FuncaoMusical salvar(FuncaoMusical funcaoMusical) {
        if (funcaoMusical.getNome() == null || funcaoMusical.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome da função.");
        }

        String nome = funcaoMusical.getNome().trim();

        boolean nomeEmUso = funcaoMusical.getId() == null
                ? funcaoMusicalRepository.existsByNomeIgnoreCase(nome)
                : funcaoMusicalRepository.existsByNomeIgnoreCaseAndIdNot(nome, funcaoMusical.getId());

        if (nomeEmUso) {
            throw new RegraNegocioException("Essa função musical já está cadastrada.");
        }

        funcaoMusical.setNome(nome);
        return funcaoMusicalRepository.save(funcaoMusical);
    }

    public void desativar(Long id) {
        alterarStatus(id, false);
    }

    public void ativar(Long id) {
        alterarStatus(id, true);
    }

    private void alterarStatus(Long id, boolean ativo) {
        FuncaoMusical funcao = buscarPorId(id);
        funcao.setAtivo(ativo);
        funcaoMusicalRepository.save(funcao);
    }
}