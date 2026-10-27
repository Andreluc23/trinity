package com.trinity.trinity.security;

import com.trinity.trinity.model.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmissorDeTokens {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public EmissorDeTokens(JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public void emitirNoLogin(String email, HttpServletRequest request, HttpServletResponse response) {
        gravarCookies(email, refreshTokenService.criar(email), request, response);
    }

    public Optional<String> renovar(HttpServletRequest request, HttpServletResponse response) {
        return JwtCookie.ler(request, JwtCookie.REFRESH)
                .flatMap(refreshTokenService::renovar)
                .map(novoRefresh -> {
                    String email = novoRefresh.getUsuario().getMembro().getEmail();
                    gravarCookies(email, novoRefresh, request, response);
                    return email;
                });
    }

    private void gravarCookies(String email, RefreshToken refresh,
                               HttpServletRequest request, HttpServletResponse response) {
        boolean seguro = request.isSecure();

        response.addCookie(JwtCookie.criar(JwtCookie.TOKEN, jwtService.gerarToken(email),
                jwtService.getValidade(), seguro));
        response.addCookie(JwtCookie.criar(JwtCookie.REFRESH, refresh.getToken(),
                refreshTokenService.getValidade(), seguro));
    }
    public void revogar(HttpServletRequest request) {
        JwtCookie.ler(request, JwtCookie.REFRESH)
                .ifPresent(refreshTokenService::revogar);
    }
}