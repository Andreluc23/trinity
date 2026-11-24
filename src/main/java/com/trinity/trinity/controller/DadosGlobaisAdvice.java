package com.trinity.trinity.controller;


import com.trinity.trinity.service.financeiro.ContribuicaoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;

/**
 * Disponibiliza para todas as telas dados usados no menu.
 */
@ControllerAdvice
public class DadosGlobaisAdvice {

    private static final Set<String> GERENCIA_FINANCEIRO = Set.of("ROLE_ADMIN", "GERENCIAR_FINANCEIRO");

    private final ContribuicaoService contribuicaoService;

    public DadosGlobaisAdvice(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    /**
     * Quantidade de contribuições aguardando confirmação, para o selo do menu (só para quem gerencia o
     * financeiro).
     */
    @ModelAttribute("contribuicoesAguardando")
    public long contribuicoesAguardando(Authentication authentication) {
        if (authentication == null || !gerenciaFinanceiro(authentication)) {
            return 0;
        }
        return contribuicaoService.contarAguardandoConfirmacao();
    }

    /** Informa se o usuário logado é administrador ou tem a permissão de gerenciar o financeiro. */
    private boolean gerenciaFinanceiro(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(GERENCIA_FINANCEIRO::contains);
    }
}