package com.trinity.trinity.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

public final class Permissoes {

    private Permissoes() {
    }

    /** Informa se o usuário logado tem pelo menos uma das permissões informadas. */
    public static boolean possuiAlguma(Authentication authentication, String... permissoes) {
        if (authentication == null) {
            return false;
        }

        Set<String> procuradas = Set.of(permissoes);

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(procuradas::contains);
    }
}