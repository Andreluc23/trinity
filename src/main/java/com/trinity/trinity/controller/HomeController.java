package com.trinity.trinity.controller;

import com.trinity.trinity.model.financeiro.TipoLancamento;
import com.trinity.trinity.security.Permissoes;
import com.trinity.trinity.service.avisos.AvisoService;
import com.trinity.trinity.service.eventos.EventoService;
import com.trinity.trinity.service.financeiro.LancamentoService;
import com.trinity.trinity.service.financeiro.ResumoMensal;
import com.trinity.trinity.service.membros.MembroService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

    /** Exibe a página inicial pública do sistema. */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** Exibe a tela de login (a autenticação em si é feita pelo Spring Security). */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Monta o painel inicial: avisos e eventos para todos; dados de membros e financeiro só para quem tem
     * permissão.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("avisosRecentes", avisoService.listarRecentes());
        model.addAttribute("proximosEventos", eventoService.listarProximos());

        if (Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_MEMBROS")) {
            model.addAttribute("totalMembros", membroService.contarTodos());
            model.addAttribute("membrosAtivos", membroService.contarAtivos());
            model.addAttribute("membrosInativos", membroService.contarInativos());

            long total = membroService.contarTodos();
            long percentualAtivos = total == 0 ? 0 : Math.round(membroService.contarAtivos() * 100.0 / total);

            model.addAttribute("comAcesso", membroService.contarComAcesso());
            model.addAttribute("percentualAtivos", percentualAtivos);
            model.addAttribute("percentualInativos", total == 0 ? 0 : 100 - percentualAtivos);
            model.addAttribute("hoje", LocalDate.now());
            model.addAttribute("aniversariantes", membroService.aniversariantesDoMes(YearMonth.now()));
        }

        if (Permissoes.possuiAlguma(authentication, "ROLE_ADMIN", "GERENCIAR_FINANCEIRO")) {
            List<ResumoMensal> resumo = lancamentoService.resumoUltimosMeses(MESES_NO_GRAFICO);

            YearMonth mesAtual = YearMonth.now();

            model.addAttribute("saldoGeral", lancamentoService.saldoGeral());
            model.addAttribute("saldoMes", lancamentoService.somarDoMes(TipoLancamento.ENTRADA, mesAtual)
                    .subtract(lancamentoService.somarDoMes(TipoLancamento.SAIDA, mesAtual)));
            model.addAttribute("rotulosMeses", resumo.stream().map(r -> r.mes().format(ROTULO_MES)).toList());
            model.addAttribute("entradasMeses", resumo.stream().map(ResumoMensal::entradas).toList());
            model.addAttribute("saidasMeses", resumo.stream().map(ResumoMensal::saidas).toList());
        }

        return "dashboard";
    }
}