package ca.yorku.greendriveev.identity.service;

import ca.yorku.greendriveev.model.User;

import java.util.Optional;

public interface JwtService {

    String generateToken(User user);

    /** Verifies signature and expiry. Empty if the token is malformed, tampered or expired. */
    Optional<TokenClaims> parse(String token);

    long expirationSeconds();
}
