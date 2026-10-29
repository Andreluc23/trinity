package com.trinity.trinity.controller;

import com.trinity.trinity.service.ContribuicaoService;
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

    @ModelAttribute("contribuicoesAguardando")
    public long contribuicoesAguardando(Authentication authentication) {
        if (authentication == null || !gerenciaFinanceiro(authentication)) {
            return 0;
        }
        return contribuicaoService.contarAguardandoConfirmacao();
    }

    private boolean gerenciaFinanceiro(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(GERENCIA_FINANCEIRO::contains);
    }
}