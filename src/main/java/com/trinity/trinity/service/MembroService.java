package com.trinity.trinity.service;

import com.trinity.trinity.model.Membro;
import com.trinity.trinity.repository.MembroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembroService {

    private final MembroRepository membroRepository;

    public MembroService(MembroRepository membroRepository) {
        this.membroRepository = membroRepository;
    }

    public List<Membro> listarTodos() {
        return membroRepository.findAll();
    }

    public Membro salvar(Membro membro) {
        return membroRepository.save(membro);
    }
}