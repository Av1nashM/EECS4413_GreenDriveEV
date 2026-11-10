package ca.yorku.greendriveev.identity.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserDTO user) {
}
