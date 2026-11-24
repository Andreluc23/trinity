package com.trinity.trinity.service.financeiro;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.exception.RegraNegocioException;

import com.trinity.trinity.model.financeiro.CategoriaFinanceira;
import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.repository.financeiro.CategoriaFinanceiraRepository;
import com.trinity.trinity.repository.financeiro.ContribuicaoRepository;
import com.trinity.trinity.repository.financeiro.LancamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ContribuicaoRepository contribuicaoRepository;

    public CategoriaFinanceiraService(CategoriaFinanceiraRepository categoriaRepository, LancamentoRepository lancamentoRepository,
                                      ContribuicaoRepository contribuicaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.contribuicaoRepository = contribuicaoRepository;
    }

    /** Todas as categorias, ordenadas por tipo e nome. */
    public List<CategoriaFinanceira> listarTodas() {
        return categoriaRepository.findAllByOrderByTipoAscNomeAsc();
    }

    /** Categorias ativas de um tipo (entrada ou saída). */
    public List<CategoriaFinanceira> listarAtivasPorTipo(TipoLancamento tipo) {
        return categoriaRepository.findByTipoAndAtivoTrueOrderByNomeAsc(tipo);
    }

    /** Busca uma categoria; se não existir, lança erro de recurso não encontrado. */
    public CategoriaFinanceira buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }

    /** Salva a categoria, exigindo nome e tipo e impedindo nomes repetidos. */
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

    /** Marca a categoria como inativa. */
    public void desativar(Long id) {
        alterarStatus(id, false);
    }

    /** Marca a categoria como ativa. */
    public void ativar(Long id) {
        alterarStatus(id, true);
    }

    /** Exclui a categoria, a menos que já tenha sido usada em lançamentos ou contribuições. */
    public void excluir(Long id) {
        CategoriaFinanceira categoria = buscarPorId(id);

        if (lancamentoRepository.existsByCategoriaId(id) || contribuicaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "A categoria \"" + categoria.getNome() + "\" já foi usada em lançamentos ou contribuições. Use Desativar.");
        }

        categoriaRepository.delete(categoria);
    }
    /** Troca o status ativo/inativo da categoria. */
    private void alterarStatus(Long id, boolean ativo) {
        CategoriaFinanceira categoria = buscarPorId(id);
        categoria.setAtivo(ativo);
        categoriaRepository.save(categoria);
    }
}