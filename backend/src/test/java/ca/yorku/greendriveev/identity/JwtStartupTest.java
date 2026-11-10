package ca.yorku.greendriveev.identity;

import ca.yorku.greendriveev.GreendriveevApplication;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.*;

/** The app must fail fast, with a clear message, when the JWT secret is missing or weak. */
class JwtStartupTest {

    private static String rootMessages(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (; t != null; t = t.getCause()) sb.append(t.getMessage()).append('\n');
        return sb.toString();
    }

    @Test
    void missingSecret_failsStartup_mentioningJwtSecret() {
        Assumptions.assumeTrue(System.getenv("JWT_SECRET") == null,
                "JWT_SECRET is set in this environment");
        Exception ex = assertThrows(Exception.class, () ->
                new SpringApplicationBuilder(GreendriveevApplication.class)
                        .profiles("test")
                        .run("--server.port=0", "--app.jwt.secret=${JWT_SECRET}"));
        assertTrue(rootMessages(ex).contains("JWT_SECRET"), rootMessages(ex));
    }

    @Test
    void shortSecret_failsStartup_withClearMessage() {
        Exception ex = assertThrows(Exception.class, () ->
                new SpringApplicationBuilder(GreendriveevApplication.class)
                        .profiles("test")
                        .run("--server.port=0", "--app.jwt.secret=too-short"));
        assertTrue(rootMessages(ex).contains("at least 32 characters"), rootMessages(ex));
    }
}
