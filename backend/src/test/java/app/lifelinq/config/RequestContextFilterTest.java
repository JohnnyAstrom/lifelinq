package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.application.ResolveHouseholdForUserUseCase;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestContextFilterTest {
    private static final String SECRET = "test-secret";

    @Test
    void setsContextFromHeadersAndClearsAfterChain() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(new FakeMembershipRepository()
                        .withMembership(userId, householdId))
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = createToken(userId, Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertEquals(householdId, context.getHouseholdId());
            assertEquals(userId, context.getUserId());
        };

        filter.doFilter(request, response, chain);

        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void clearsContextWhenChainThrows() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(new FakeMembershipRepository()
                        .withMembership(userId, householdId))
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = createToken(userId, Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));

        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenTokenMissing() throws Exception {
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(new FakeMembershipRepository())
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenTokenInvalid() throws Exception {
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(new FakeMembershipRepository())
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer not-a-token");

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void proceedsWhenHouseholdMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(new FakeMembershipRepository())
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = createToken(userId, Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertNull(context.getHouseholdId());
            assertEquals(userId, context.getUserId());
        };

        filter.doFilter(request, response, chain);

        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenHouseholdResolutionAmbiguous() throws Exception {
        UUID userId = UUID.randomUUID();
        FakeMembershipRepository repository = new FakeMembershipRepository()
                .withMemberships(userId, List.of(UUID.randomUUID(), UUID.randomUUID()));
        RequestContextFilter filter = new RequestContextFilter(
                new JwtVerifier(SECRET),
                new ResolveHouseholdForUserUseCase(repository)
        );
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = createToken(userId, Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verifyNoInteractions(chain);
        assertNull(RequestContextHolder.getCurrent());
    }

    private String createToken(UUID userId, Instant exp) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"userId\":\"%s\",\"exp\":%d}",
                userId,
                exp.getEpochSecond()
        );
        String headerPart = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signaturePart = base64Url(hmacSha256(headerPart + "." + payloadPart));
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    private byte[] hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static final class FakeMembershipRepository implements MembershipRepository {
        private final Map<UUID, List<UUID>> byUser = new HashMap<>();

        FakeMembershipRepository withMembership(UUID userId, UUID householdId) {
            return withMemberships(userId, List.of(householdId));
        }

        FakeMembershipRepository withMemberships(UUID userId, List<UUID> householdIds) {
            byUser.put(userId, householdIds);
            return this;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            return byUser.getOrDefault(userId, List.of());
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
