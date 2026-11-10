package ca.yorku.greendriveev.identity.security;

import ca.yorku.greendriveev.identity.repository.RevokedTokenRepository;
import ca.yorku.greendriveev.identity.service.JwtService;
import ca.yorku.greendriveev.identity.service.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates /api/v1/** requests that carry a valid, non-revoked Bearer token.
 * It never rejects a request itself: no/invalid token just means "anonymous", and
 * SecurityConfig decides which routes need authentication. Everything outside
 * /api/v1/ (the old API the React app uses) is passed through untouched.
 *
 * Deliberately NOT a @Component, so Boot does not register it a second time.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokens;

    public JwtAuthFilter(JwtService jwtService, RevokedTokenRepository revokedTokens) {
        this.jwtService = jwtService;
        this.revokedTokens = revokedTokens;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            jwtService.parse(header.substring(PREFIX.length()).trim())
                    .filter(t -> !revokedTokens.existsById(t.jti()))
                    .ifPresent(this::authenticate);
        }
        chain.doFilter(request, response);
    }

    private void authenticate(TokenClaims claims) {
        var auth = new UsernamePasswordAuthenticationToken(
                claims, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
