package ca.yorku.greendriveev.identity.service;

import ca.yorku.greendriveev.model.Role;

import java.time.Instant;

/** The verified contents of a JWT. Also used as the Spring Security principal. */
public record TokenClaims(String jti, Long userId, String email, Role role, Instant expiresAt) {
}
