package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.CategoriaFinanceira;
import com.trinity.trinity.model.TipoLancamento;
import com.trinity.trinity.repository.CategoriaFinanceiraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository categoriaRepository;

    public CategoriaFinanceiraService(CategoriaFinanceiraRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaFinanceira> listarTodas() {
        return categoriaRepository.findAllByOrderByTipoAscNomeAsc();
    }

    public List<CategoriaFinanceira> listarAtivasPorTipo(TipoLancamento tipo) {
        return categoriaRepository.findByTipoAndAtivoTrueOrderByNomeAsc(tipo);
    }

    public CategoriaFinanceira buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }

    public CategoriaFinanceira salvar(CategoriaFinanceira categoria) {
        if (categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }

        if (categoria.getTipo() == null) {
            throw new RegraNegocioException("Selecione o tipo da categoria.");
        }
        String nome = categoria.getNome().trim();

        boolean nomeEmUso = categoria.getId() == null
                ? categoriaRepository.existsByNomeIgnoreCase(nome)
                : categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, categoria.getId());

        if (nomeEmUso) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }

        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }

    public void desativar(Long id) {
        alterarStatus(id, false);
    }

    public void ativar(Long id) {
        alterarStatus(id, true);
    }

    private void alterarStatus(Long id, boolean ativo) {
        CategoriaFinanceira categoria = buscarPorId(id);
        categoria.setAtivo(ativo);
        categoriaRepository.save(categoria);
    }
}