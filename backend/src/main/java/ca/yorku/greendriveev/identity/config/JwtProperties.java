package ca.yorku.greendriveev.identity.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** JWT settings. The secret has no default: the app refuses to start without it. */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT_SECRET must be set")
        @Size(min = 32, message = "JWT_SECRET must be at least 32 characters")
        String secret,
        @Min(value = 1, message = "app.jwt.expiration-minutes must be at least 1")
        long expirationMinutes) {
}
