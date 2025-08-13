package com.challenge.AluraForum.config;

import com.challenge.AluraForum.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-seconds}")
    private long expiration;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String gerarToken(String subject, String sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .claim("sessionId", sessionId) // adiciona info extra no JWT
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiration)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String getSessionId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("sessionId", String.class);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("Token expirado. Faça login novamente.");
        }
    }

    public String validarEGetSubject(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return jws.getBody().getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("Token expirado. Faça login novamente.");
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException("Formato de token não suportado.");
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException("Token inválido.");
        } catch (SignatureException e) {
            throw new UnauthorizedException("Assinatura do token inválida.");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Token ausente ou vazio.");
        }
    }
}
