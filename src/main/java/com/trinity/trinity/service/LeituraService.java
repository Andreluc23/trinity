package com.trinity.trinity.service;

import com.trinity.trinity.exception.RegraNegocioException;
import com.trinity.trinity.model.Aviso;
import com.trinity.trinity.model.LeituraAviso;
import com.trinity.trinity.model.Membro;
import com.trinity.trinity.repository.LeituraAvisoRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeituraService {

    private final LeituraAvisoRepository leituraRepository;
    private final AvisoService avisoService;
    private final UsuarioService usuarioService;

    public LeituraService(LeituraAvisoRepository leituraRepository,
                          AvisoService avisoService,
                          UsuarioService usuarioService) {
        this.leituraRepository = leituraRepository;
        this.avisoService = avisoService;
        this.usuarioService = usuarioService;
    }

    public void confirmar(Long avisoId, String emailMembro) {
        Aviso aviso = avisoService.buscarPorId(avisoId);

        if (!aviso.isAtivo()) {
            throw new RegraNegocioException("Este aviso não está mais disponível.");
        }

        Membro membro = usuarioService.buscarPorEmail(emailMembro).getMembro();

        if (!leituraRepository.existsByAvisoIdAndMembroId(avisoId, membro.getId())) {
            leituraRepository.save(new LeituraAviso(aviso, membro));
        }
    }

    /**
     * Ids dos avisos que o membro logado já confirmou ter lido.
     */
    public Set<Long> avisosLidos(String emailMembro) {
        Long membroId = usuarioService.buscarPorEmail(emailMembro).getMembro().getId();

        return leituraRepository.findByMembroId(membroId).stream()
                .map(leitura -> leitura.getAviso().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Quantas pessoas leram cada aviso da lista.
     */
    public Map<Long, Long> contarPorAviso(List<Aviso> avisos) {
        if (avisos.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = avisos.stream().map(Aviso::getId).toList();

        return leituraRepository.findByAvisoIdIn(ids).stream()
                .collect(Collectors.groupingBy(leitura -> leitura.getAviso().getId(), Collectors.counting()));
    }

    public List<LeituraAviso> listarDoAviso(Long avisoId) {
        return leituraRepository.findByAvisoIdOrderByLidoEmAsc(avisoId);
    }

    /**
     * Membros ativos, com conta ativa, que ainda não confirmaram a leitura.
     */
    public List<Membro> listarQuemNaoLeu(Long avisoId) {
        Set<Long> leram = listarDoAviso(avisoId).stream()
                .map(leitura -> leitura.getMembro().getId())
                .collect(Collectors.toSet());

        return membrosComAcesso().stream()
                .filter(membro -> !leram.contains(membro.getId()))
                .sorted(Comparator.comparing(Membro::getNomeCompleto))
                .toList();
    }
    /**
     * Membros que podem confirmar leitura: ativos e com conta de acesso ativa.
     */
    private List<Membro> membrosComAcesso() {
        return usuarioService.listarTodos().stream()
                .filter(usuario -> usuario.isAtivo() && usuario.getMembro().isAtivo())
                .map(usuario -> usuario.getMembro())
                .toList();
    }

    public long contarLeitoresPossiveis() {
        return membrosComAcesso().size();
    }
}