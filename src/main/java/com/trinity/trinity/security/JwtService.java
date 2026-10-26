package com.trinity.trinity.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey chave;
    private final Duration validade;

    public JwtService(@Value("${trinity.jwt.segredo}") String segredo,
                      @Value("${trinity.jwt.validade-minutos}") long validadeMinutos) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredo));
        this.validade = Duration.ofMinutes(validadeMinutos);
    }

    public String gerarToken(String email) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave)
                .compact();
    }

    public Optional<String> extrairEmail(String token) {
        try {
            String email = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return Optional.of(email);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    public Duration getValidade() {
        return validade;
    }
}