package ca.yorku.greendriveev.identity.service;

import ca.yorku.greendriveev.identity.config.JwtProperties;
import ca.yorku.greendriveev.model.Role;
import ca.yorku.greendriveev.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtServiceImpl(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = props.expirationMinutes() * 60;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        Role role = user.getRole() == null ? Role.CUSTOMER : user.getRole();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<TokenClaims> parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return Optional.of(new TokenClaims(
                    c.getId(),
                    Long.valueOf(c.getSubject()),
                    c.get("email", String.class),
                    Role.valueOf(c.get("role", String.class)),
                    c.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public long expirationSeconds() {
        return expirationSeconds;
    }
}
