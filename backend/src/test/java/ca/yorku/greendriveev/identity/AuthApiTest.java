package ca.yorku.greendriveev.identity;

import ca.yorku.greendriveev.model.User;
import ca.yorku.greendriveev.repository.CartRepository;
import ca.yorku.greendriveev.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTest {

    private static final String PASSWORD = "correct-horse-1";
    private static final String REACT_ORIGIN = "https://eecs4413-greendriveev-react.onrender.com";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired CartRepository carts;

    private static String email() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static String registerJson(String email, String password) {
        return "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void register(String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(email, PASSWORD))).andExpect(status().isCreated());
    }

    private String loginToken(String email) throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
    }

    // ---- TC1: register ----

    @Test
    void registerValid_returns201_andStoresHashedPassword() throws Exception {
        String email = email();
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User saved = users.findByEmailIgnoreCase(email).orElseThrow();
        assertNotEquals(PASSWORD, saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("$2"), "expected a BCrypt hash");
        assertTrue(carts.findByUserId(saved.getId()).isPresent(), "register should create a cart");
    }

    @Test
    void register_ignoresRoleInBody_alwaysCustomer() throws Exception {
        String email = email();
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Evil\",\"email\":\"" + email
                                + "\",\"password\":\"" + PASSWORD + "\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void registerInvalid_returns400_inStandardErrorFormat() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("not-an-email", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // ---- TC2: duplicate email ----

    @Test
    void registerDuplicateEmail_returns409_caseInsensitive() throws Exception {
        String email = email();
        register(email);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    // ---- TC3: login ----

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String email = email();
        register(email);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_correctPassword_returns200_andToken() throws Exception {
        String email = email();
        register(email);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    // ---- TC4: logout invalidates the token ----

    @Test
    void logout_returns204_thenSameTokenIsRejectedByMe() throws Exception {
        String email = email();
        register(email);
        String token = loginToken(email);

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withoutToken_returns401() throws Exception {
        mvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/logout"));
    }

    @Test
    void me_withoutToken_or_withGarbageToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutTwice_secondCallIs401_notAServerError() throws Exception {
        String email = email();
        register(email);
        String token = loginToken(email);
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // ---- old API must behave exactly as before ----

    @Test
    void oldAuthLogin_stillWorks_withoutAnyToken() throws Exception {
        String email = email();
        register(email);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void oldRoutes_ignoreABogusAuthorizationHeader() throws Exception {
        mvc.perform(get("/api/vehicles").header("Authorization", "Bearer garbage"))
                .andExpect(status().isOk());
    }

    // ---- CORS ----

    @Test
    void preflightToNewLogin_allowsReactOrigin() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                        .header("Origin", REACT_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", REACT_ORIGIN));
    }

    @Test
    void preflightToProtectedNewRoute_isAnsweredBeforeAuth() throws Exception {
        mvc.perform(options("/api/v1/auth/logout")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void preflightToNewLogin_rejectsUnknownOrigin() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void oldControllers_stillAllowAnyOrigin() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "https://anything.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
        mvc.perform(get("/api/vehicles").header("Origin", "https://anything.example.com"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
