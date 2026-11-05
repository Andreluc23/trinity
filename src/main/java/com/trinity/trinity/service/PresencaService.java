package com.trinity.trinity.service;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Evento;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.model.PresencaEvento;
import com.trinity.trinity.model.RespostaPresenca;
import com.trinity.trinity.repository.PresencaEventoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PresencaService {

    private final PresencaEventoRepository presencaRepository;
    private final EventoService eventoService;
    private final UsuarioService usuarioService;

    public PresencaService(PresencaEventoRepository presencaRepository,
                           EventoService eventoService,
                           UsuarioService usuarioService) {
        this.presencaRepository = presencaRepository;
        this.eventoService = eventoService;
        this.usuarioService = usuarioService;
    }

    public void responder(Long eventoId, String emailMembro, RespostaPresenca resposta) {
        if (resposta == null) {
            throw new RegraNegocioException("Resposta inválida.");
        }

        Evento evento = eventoService.buscarPorId(eventoId);

        if (!evento.isAtivo() || evento.getData().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Não é mais possível responder a este evento.");
        }

        Membro membro = usuarioService.buscarPorEmail(emailMembro).getMembro();

        PresencaEvento presenca = presencaRepository.findByEventoIdAndMembroId(eventoId, membro.getId())
                .orElseGet(() -> new PresencaEvento(evento, membro, resposta));

        presenca.responder(resposta);
        presencaRepository.save(presenca);
    }

    /**
     * Respostas do membro logado, no formato "id do evento → resposta".
     */
    public Map<Long, RespostaPresenca> respostasDoMembro(String emailMembro) {
        Long membroId = usuarioService.buscarPorEmail(emailMembro).getMembro().getId();

        return presencaRepository.findByMembroId(membroId).stream()
                .collect(Collectors.toMap(p -> p.getEvento().getId(), PresencaEvento::getResposta));
    }

    /**
     * Quantos vão e quantos não vão, para cada evento da lista.
     */
    public Map<Long, ContagemPresenca> contarPorEvento(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = eventos.stream().map(Evento::getId).toList();
        Map<Long, ContagemPresenca> contagens = new HashMap<>();

        for (PresencaEvento presenca : presencaRepository.findByEventoIdIn(ids)) {
            contagens.merge(presenca.getEvento().getId(),
                    ContagemPresenca.de(presenca.getResposta()),
                    ContagemPresenca::somar);
        }

        return contagens;
    }

    public List<PresencaEvento> listarDoEvento(Long eventoId) {
        return presencaRepository.findByEventoIdOrderByMembroNomeCompletoAsc(eventoId);
    }
}