package ca.yorku.greendriveev.identity.repository;

import ca.yorku.greendriveev.identity.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    /** Expired tokens are rejected by signature/expiry anyway, so their rows can go. */
    @Transactional
    long deleteByExpiresAtBefore(Instant cutoff);
}
