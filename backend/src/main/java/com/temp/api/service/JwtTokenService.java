package com.temp.api.service;

import com.temp.api.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration validade;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer:climate-app}") String issuer,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.validade = Duration.ofMinutes(expirationMinutes);
    }

    public String gerarToken(User user) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getValidadeEmSegundos() {
        return validade.toSeconds();
    }
}