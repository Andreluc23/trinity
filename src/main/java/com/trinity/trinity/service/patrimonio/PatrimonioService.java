package com.trinity.trinity.service.patrimonio;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;

import com.trinity.trinity.model.patrimonio.Patrimonio;
import com.trinity.trinity.repository.patrimonio.PatrimonioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
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
    private static final int ITENS_POR_PAGINA = 12;

    public Page<Patrimonio> buscar(String termo, int pagina) {
        Pageable pageable = PageRequest.of(pagina, ITENS_POR_PAGINA, Sort.by("nome"));

        if (termo == null || termo.isBlank()) {
            return patrimonioRepository.findAll(pageable);
        }

        return patrimonioRepository
                .findByNomeContainingIgnoreCaseOrCategoriaContainingIgnoreCaseOrLocalizacaoContainingIgnoreCase(
                        termo, termo, termo, pageable);
    }

    public Patrimonio buscarPorId(Long id) {
        return patrimonioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Patrimônio não encontrado"));
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
    public void excluir(Long id) {
        patrimonioRepository.delete(buscarPorId(id));
    }

}