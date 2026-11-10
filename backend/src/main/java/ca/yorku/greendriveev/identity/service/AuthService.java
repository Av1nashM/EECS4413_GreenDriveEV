package ca.yorku.greendriveev.identity.service;

import ca.yorku.greendriveev.identity.dto.AuthResponse;
import ca.yorku.greendriveev.identity.dto.LoginRequest;
import ca.yorku.greendriveev.identity.dto.RegisterRequest;
import ca.yorku.greendriveev.identity.dto.UserDTO;

/** Facade over the identity module: controllers talk only to this interface. */
public interface AuthService {

    /** @throws ca.yorku.greendriveev.identity.exception.EmailAlreadyExistsException */
    UserDTO register(RegisterRequest request);

    /** @throws ca.yorku.greendriveev.identity.exception.InvalidCredentialsException */
    AuthResponse login(LoginRequest request);

    /** Blocklists the token until it would have expired anyway. */
    void logout(TokenClaims token);

    UserDTO currentUser(Long userId);
}
