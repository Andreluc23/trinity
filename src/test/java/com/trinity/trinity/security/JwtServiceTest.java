package com.trinity.trinity.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SEGREDO =
            "MpvcgrLO3qLIqxFm2ccTBR9CW/kXU19xr+eNgmohvkTf1FBXv6HDdlxhXZkL6Hw9";

    private final JwtService jwtService = new JwtService(SEGREDO, 15);

    @Test
    void deveGerarTokenEExtrairOEmail() {
        String token = jwtService.gerarToken("membro@teste.com");

        assertEquals("membro@teste.com", jwtService.extrairEmail(token).orElseThrow());
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken("membro@teste.com");
        String adulterado = token.substring(0, token.length() - 2) + "xx";

        assertTrue(jwtService.extrairEmail(adulterado).isEmpty());
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService servicoExpirado = new JwtService(SEGREDO, -1);
        String token = servicoExpirado.gerarToken("membro@teste.com");

        assertTrue(jwtService.extrairEmail(token).isEmpty());
    }
}