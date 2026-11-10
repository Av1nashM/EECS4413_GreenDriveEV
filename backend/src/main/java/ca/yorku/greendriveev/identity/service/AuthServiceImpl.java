package ca.yorku.greendriveev.identity.service;

import ca.yorku.greendriveev.identity.dto.AuthResponse;
import ca.yorku.greendriveev.identity.dto.LoginRequest;
import ca.yorku.greendriveev.identity.dto.RegisterRequest;
import ca.yorku.greendriveev.identity.dto.UserDTO;
import ca.yorku.greendriveev.identity.entity.RevokedToken;
import ca.yorku.greendriveev.identity.exception.EmailAlreadyExistsException;
import ca.yorku.greendriveev.identity.exception.InvalidCredentialsException;
import ca.yorku.greendriveev.identity.repository.RevokedTokenRepository;
import ca.yorku.greendriveev.model.Cart;
import ca.yorku.greendriveev.model.Role;
import ca.yorku.greendriveev.model.User;
import ca.yorku.greendriveev.repository.CartRepository;
import ca.yorku.greendriveev.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           CartRepository cartRepository,
                           RevokedTokenRepository revokedTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER); // registration never grants ADMIN

        User saved;
        try {
            saved = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // two simultaneous registrations with the same email
            throw new EmailAlreadyExistsException();
        }

        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.expirationSeconds(),
                toDto(user));
    }

    @Override
    @Transactional
    public void logout(TokenClaims token) {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
        if (!revokedTokenRepository.existsById(token.jti())) {
            revokedTokenRepository.save(new RevokedToken(token.jti(), token.expiresAt()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO currentUser(Long userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private UserDTO toDto(User u) {
        return new UserDTO(u.getId(), u.getName(), u.getEmail(),
                u.getRole() == null ? Role.CUSTOMER : u.getRole());
    }
}
