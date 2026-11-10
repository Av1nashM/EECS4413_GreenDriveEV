package ca.yorku.greendriveev.identity.controller;

import ca.yorku.greendriveev.identity.dto.AuthResponse;
import ca.yorku.greendriveev.identity.dto.LoginRequest;
import ca.yorku.greendriveev.identity.dto.RegisterRequest;
import ca.yorku.greendriveev.identity.dto.UserDTO;
import ca.yorku.greendriveev.identity.service.AuthService;
import ca.yorku.greendriveev.identity.service.TokenClaims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** PB-21 register, PB-22 login, PB-23 logout. */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class IdentityAuthController {

    private final AuthService authService;

    public IdentityAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal TokenClaims token) {
        authService.logout(token);
    }

    @GetMapping("/me")
    public UserDTO me(@AuthenticationPrincipal TokenClaims token) {
        return authService.currentUser(token.userId());
    }
}
