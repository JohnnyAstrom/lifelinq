package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class OAuth2LoginSuccessHandlerTest {
    private static final String SECRET = "test-secret";

    @Test
    void issuesTokenAndEnsuresUserExists() throws Exception {
        RecordingUserRepository userRepository = new RecordingUserRepository();
        EnsureUserExistsUseCase ensureUserExistsUseCase = new EnsureUserExistsUseCase(userRepository);
        JwtSigner signer = new JwtSigner(SECRET, 300, null, null, Clock.fixed(Instant.now(), ZoneOffset.UTC));
        ObjectMapper objectMapper = new ObjectMapper();
        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                ensureUserExistsUseCase,
                signer,
                objectMapper
        );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "provider-sub");
        OAuth2User user = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "google"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals(200, response.getStatus());
        Map<String, String> payload = objectMapper.readValue(
                response.getContentAsString(),
                new TypeReference<Map<String, String>>() {}
        );
        String token = payload.get("token");
        assertNotNull(token);

        UUID expectedUserId = OAuth2LoginSuccessHandler.deterministicUserId("google", "provider-sub");
        assertEquals(expectedUserId, userRepository.lastSavedUserId);

        JwtClaims claims = new JwtVerifier(SECRET).verify(token);
        assertEquals(expectedUserId, claims.getUserId());
    }

    private static final class RecordingUserRepository implements UserRepository {
        private final Map<UUID, User> store = new HashMap<>();
        private UUID lastSavedUserId;

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void save(User user) {
            lastSavedUserId = user.getId();
            store.put(user.getId(), user);
        }
    }
}
