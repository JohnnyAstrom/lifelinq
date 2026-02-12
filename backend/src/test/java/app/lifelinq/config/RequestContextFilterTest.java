package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;

class RequestContextFilterTest {
    private static final String SECRET = "test-secret";

    @Test
    void setsContextFromHeadersAndClearsAfterChain() throws Exception {
        EnsureUserExistsUseCase ensureUserExistsUseCase = Mockito.mock(EnsureUserExistsUseCase.class);
        RequestContextFilter filter = new RequestContextFilter(new JwtVerifier(SECRET), ensureUserExistsUseCase);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String token = createToken(householdId, userId, Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = (req, res) -> {
            RequestContext context = RequestContextHolder.getCurrent();
            assertEquals(householdId, context.getHouseholdId());
            assertEquals(userId, context.getUserId());
        };

        filter.doFilter(request, response, chain);

        Mockito.verify(ensureUserExistsUseCase).execute(userId);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void clearsContextWhenChainThrows() throws Exception {
        EnsureUserExistsUseCase ensureUserExistsUseCase = Mockito.mock(EnsureUserExistsUseCase.class);
        RequestContextFilter filter = new RequestContextFilter(new JwtVerifier(SECRET), ensureUserExistsUseCase);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        String token = createToken(UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(60));
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));

        Mockito.verify(ensureUserExistsUseCase).execute(Mockito.any(UUID.class));
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenTokenMissing() throws Exception {
        EnsureUserExistsUseCase ensureUserExistsUseCase = Mockito.mock(EnsureUserExistsUseCase.class);
        RequestContextFilter filter = new RequestContextFilter(new JwtVerifier(SECRET), ensureUserExistsUseCase);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verifyNoInteractions(chain);
        Mockito.verifyNoInteractions(ensureUserExistsUseCase);
        assertNull(RequestContextHolder.getCurrent());
    }

    @Test
    void returnsUnauthorizedWhenTokenInvalid() throws Exception {
        EnsureUserExistsUseCase ensureUserExistsUseCase = Mockito.mock(EnsureUserExistsUseCase.class);
        RequestContextFilter filter = new RequestContextFilter(new JwtVerifier(SECRET), ensureUserExistsUseCase);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer not-a-token");

        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verifyNoInteractions(chain);
        Mockito.verifyNoInteractions(ensureUserExistsUseCase);
        assertNull(RequestContextHolder.getCurrent());
    }

    private String createToken(UUID householdId, UUID userId, Instant exp) throws Exception {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
                "{\"householdId\":\"%s\",\"userId\":\"%s\",\"exp\":%d}",
                householdId,
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
}
