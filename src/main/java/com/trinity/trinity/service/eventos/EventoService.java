package com.trinity.trinity.service.eventos;

import com.trinity.trinity.exception.RecursoNaoEncontradoException;
import com.trinity.trinity.model.eventos.Evento;
import com.trinity.trinity.repository.eventos.EventoRepository;
import com.trinity.trinity.repository.eventos.PresencaEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;



@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final PresencaEventoRepository presencaRepository;
    private static final int ITENS_POR_PAGINA = 12;

    public EventoService(EventoRepository eventoRepository, PresencaEventoRepository presencaRepository) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
    }

    public Page<Evento> listarTodos(int pagina) {
        return eventoRepository.findAllByOrderByDataDescHorarioDesc(PageRequest.of(pagina, ITENS_POR_PAGINA));
    }

    public Page<Evento> listarFuturos(int pagina) {
        return eventoRepository.findByAtivoTrueAndDataGreaterThanEqualOrderByDataAscHorarioAsc(
                LocalDate.now(), PageRequest.of(pagina, ITENS_POR_PAGINA));
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