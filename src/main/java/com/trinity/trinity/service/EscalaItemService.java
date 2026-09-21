package com.trinity.trinity.service;

import com.trinity.trinity.model.Escala;
import com.trinity.trinity.model.EscalaItem;
import com.trinity.trinity.model.FuncaoMusical;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.repository.EscalaItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalaItemService {

    private final EscalaItemRepository escalaItemRepository;

    public EscalaItemService(EscalaItemRepository escalaItemRepository) {
        this.escalaItemRepository = escalaItemRepository;
    }

    public List<EscalaItem> listarPorEscala(Long escalaId) {
        return escalaItemRepository.findByEscalaId(escalaId);
    }

    public EscalaItem salvar(
            Escala escala,
            Membro membro,
            FuncaoMusical funcaoMusical) {

        EscalaItem item = new EscalaItem();

        item.setEscala(escala);
        item.setMembro(membro);
        item.setFuncaoMusical(funcaoMusical);

        return escalaItemRepository.save(item);
    }

    public void remover(Long id) {
        escalaItemRepository.deleteById(id);
    }
    public EscalaItem buscarPorId(Long id) {
        return escalaItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Integrante da escala não encontrado"));
    }
    public EscalaItem atualizar(
            Long itemId,
            Membro membro,
            FuncaoMusical funcaoMusical) {

        EscalaItem item = buscarPorId(itemId);

        item.setMembro(membro);
        item.setFuncaoMusical(funcaoMusical);

        return escalaItemRepository.save(item);
    }
}