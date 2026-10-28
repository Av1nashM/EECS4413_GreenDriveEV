
package ca.yorku.greendriveev.controller;

import ca.yorku.greendriveev.model.Cart;
import ca.yorku.greendriveev.model.User;
import ca.yorku.greendriveev.repository.CartRepository;
import ca.yorku.greendriveev.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder =
        new BCryptPasswordEncoder();

    public AuthController(
            UserRepository userRepository,
            CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {

        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(
                Map.of("error",
                    "Name and email are required; password must be at least 8 characters.")
            );
        }

        email = email.trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "An account with this email already exists.")
            );
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Account registered successfully.",
                "userId", savedUser.getId(),
                "name", savedUser.getName(),
                "email", savedUser.getEmail()
            )
        );
    }

    
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String password = body.get("password");

    if (email == null || email.isBlank()
            || password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(
            Map.of("error", "Email and password are required.")
        );
    }

    User user = userRepository
        .findByEmailIgnoreCase(email.trim())
        .orElse(null);

    if (user == null || !passwordEncoder.matches(
            password, user.getPasswordHash())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of("error", "Invalid email or password.")
        );
    }

    return ResponseEntity.ok(Map.of(
        "message", "Login credentials are valid.",
        "userId", user.getId(),
        "name", user.getName(),
        "email", user.getEmail()
    ));
}

@PostMapping("/logout")
public ResponseEntity<?> logout() {
    return ResponseEntity.ok(
        Map.of("message", "Logout endpoint reached.")
    );
}

}
