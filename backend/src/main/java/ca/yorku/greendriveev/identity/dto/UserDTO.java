package ca.yorku.greendriveev.identity.dto;

import ca.yorku.greendriveev.model.Role;

/** Public view of a user. Never contains the password hash. */
public record UserDTO(Long id, String name, String email, Role role) {
}
