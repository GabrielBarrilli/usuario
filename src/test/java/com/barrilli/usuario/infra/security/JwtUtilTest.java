package com.barrilli.usuario.infra.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("segredo-de-teste-com-pelo-menos-32-bytes!".getBytes());

    private static JwtUtil comSegredo(String secret) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secretKey", secret);
        return util;
    }

    private final JwtUtil jwtUtil = comSegredo(SECRET);

    @Test
    void geraTokenQueValidaParaOMesmoUsuario() {
        String token = jwtUtil.generateToken("ana@t.com");

        assertThat(jwtUtil.extrairEmailToken(token)).isEqualTo("ana@t.com");
        assertThat(jwtUtil.validateToken(token, "ana@t.com")).isTrue();
        assertThat(jwtUtil.validateToken(token, "outro@t.com")).isFalse();
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void tokenExpiraEmUmaHora() {
        var claims = jwtUtil.extractClaims(jwtUtil.generateToken("ana@t.com"));

        long duracao = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(duracao).isEqualTo(60 * 60 * 1000);
    }

    @Test
    void tokenExpiradoERejeitado() {
        String expirado = Jwts.builder()
                .subject("ana@t.com")
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)))
                .compact();

        assertThatThrownBy(() -> jwtUtil.extrairEmailToken(expirado)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenDeOutroSegredoERejeitado() {
        String outroSegredo = Base64.getEncoder().encodeToString("outro-segredo-de-teste-com-32-bytes!!".getBytes());
        String token = comSegredo(outroSegredo).generateToken("ana@t.com");

        assertThatThrownBy(() -> jwtUtil.extrairEmailToken(token)).isInstanceOf(SignatureException.class);
    }
}
