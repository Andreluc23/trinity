package com.trinity.trinity.service;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.model.Evento;
import com.trinity.trinity.repository.EventoRepository;
import com.trinity.trinity.repository.PresencaEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final PresencaEventoRepository presencaRepository;

    public EventoService(EventoRepository eventoRepository, PresencaEventoRepository presencaRepository) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAllByOrderByDataDescHorarioDesc();
    }

    public List<Evento> listarFuturos() {
        return eventoRepository.findByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(LocalDate.now());
    }
    public Evento salvar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
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
    @Transactional
    public void excluir(Long id) {
        Evento evento = buscarPorId(id);
        presencaRepository.deleteByEventoId(id);
        eventoRepository.delete(evento);
    }
    public List<Evento> listarProximos() {
        return eventoRepository
                .findTop5ByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(
                        LocalDate.now()
                );
    }
}