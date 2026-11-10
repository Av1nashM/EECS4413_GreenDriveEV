
package ca.yorku.greendriveev.config;

import ca.yorku.greendriveev.identity.repository.RevokedTokenRepository;
import ca.yorku.greendriveev.identity.security.JsonAuthEntryPoint;
import ca.yorku.greendriveev.identity.security.JwtAuthFilter;
import ca.yorku.greendriveev.identity.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            RevokedTokenRepository revokedTokens,
            JsonAuthEntryPoint entryPoint) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Specific rules first. Everything else, including all old /api/**, stays public.
                .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/me").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
            .addFilterBefore(new JwtAuthFilter(jwtService, revokedTokens),
                UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
