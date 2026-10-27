package com.trinity.trinity.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public final class JwtCookie {

    public static final String TOKEN = "trinity_token";
    public static final String REFRESH = "trinity_refresh";

    private JwtCookie() {
    }

    public static Cookie criar(String nome, String valor, Duration validade, boolean seguro) {
        Cookie cookie = new Cookie(nome, valor);
        cookie.setHttpOnly(true);
        cookie.setSecure(seguro);
        cookie.setPath("/");
        cookie.setMaxAge((int) validade.toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    public static Optional<String> ler(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> nome.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}