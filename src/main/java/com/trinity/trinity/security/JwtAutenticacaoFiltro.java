package com.trinity.trinity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAutenticacaoFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAutenticacaoFiltro(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtCookie.ler(request)
                    .flatMap(jwtService::extrairEmail)
                    .ifPresent(this::autenticar);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String email) {
        try {
            UserDetails usuario = userDetailsService.loadUserByUsername(email);

            if (usuario.isEnabled()) {
                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                usuario, null, usuario.getAuthorities()));
            }
        } catch (UsernameNotFoundException e) {
            // Usuário removido do banco: segue sem autenticar
        }
    }
}