package com.trinity.trinity.controller;

import com.trinity.trinity.service.AvisoService;
import com.trinity.trinity.service.EventoService;
import com.trinity.trinity.service.LancamentoService;
import com.trinity.trinity.service.MembroService;
import com.trinity.trinity.service.ResumoMensal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
public class HomeController {

    private static final int MESES_NO_GRAFICO = 6;
    private static final DateTimeFormatter ROTULO_MES =
            DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));

    private final MembroService membroService;
    private final AvisoService avisoService;
    private final EventoService eventoService;
    private final LancamentoService lancamentoService;

    public HomeController(MembroService membroService,
                          AvisoService avisoService,
                          EventoService eventoService,
                          LancamentoService lancamentoService) {
        this.membroService = membroService;
        this.avisoService = avisoService;
        this.eventoService = eventoService;
        this.lancamentoService = lancamentoService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("avisosRecentes", avisoService.listarRecentes());
        model.addAttribute("proximosEventos", eventoService.listarProximos());

        if (possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_MEMBROS")) {
            model.addAttribute("totalMembros", membroService.contarTodos());
            model.addAttribute("membrosAtivos", membroService.contarAtivos());
            model.addAttribute("membrosInativos", membroService.contarInativos());
        }

        if (possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_FINANCEIRO")) {
            List<ResumoMensal> resumo = lancamentoService.resumoUltimosMeses(MESES_NO_GRAFICO);

            model.addAttribute("saldoGeral", lancamentoService.saldoGeral());
            model.addAttribute("rotulosMeses", resumo.stream().map(r -> r.mes().format(ROTULO_MES)).toList());
            model.addAttribute("entradasMeses", resumo.stream().map(ResumoMensal::entradas).toList());
            model.addAttribute("saidasMeses", resumo.stream().map(ResumoMensal::saidas).toList());
        }

        return "dashboard";
    }

    private boolean possuiAlguma(Authentication authentication, String... permissoes) {
        Set<String> procuradas = Set.of(permissoes);
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(procuradas::contains);
    }
}