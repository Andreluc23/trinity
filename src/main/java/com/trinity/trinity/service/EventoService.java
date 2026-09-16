package com.trinity.trinity.service;

import com.trinity.trinity.model.Evento;
import com.trinity.trinity.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento salvar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    public void cancelar(Long id) {
        Evento evento = buscarPorId(id);
        evento.setAtivo(false);
        eventoRepository.save(evento);
    }

    public void ativar(Long id) {
        Evento evento = buscarPorId(id);
        evento.setAtivo(true);
        eventoRepository.save(evento);
    }
    public List<Evento> listarProximos() {
        return eventoRepository
                .findTop5ByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(
                        LocalDate.now()
                );
    }
}