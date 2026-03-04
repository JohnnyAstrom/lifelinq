package app.lifelinq.config;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.application.AuthTokenPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthApplicationService authApplicationService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(
            AuthApplicationService authApplicationService,
            ObjectMapper objectMapper
    ) {
        this.authApplicationService = authApplicationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        OAuth2User user = token.getPrincipal();
        String provider = token.getAuthorizedClientRegistrationId();
        String subject = resolveSubject(user);
        String normalizedEmail = normalizeEmailOrNull(resolveEmail(user));
        boolean emailVerified = resolveEmailVerified(user);

        AuthTokenPair tokens = authApplicationService.issueAuthPairForOAuthLogin(
                provider,
                subject,
                normalizedEmail,
                emailVerified
        );
        Map<String, String> payload = new HashMap<>();
        payload.put("accessToken", tokens.accessToken());
        payload.put("refreshToken", tokens.refreshToken());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), payload);
    }

    private String resolveSubject(OAuth2User user) {
        Object sub = user.getAttributes().get("sub");
        if (sub != null) {
            return String.valueOf(sub);
        }
        Object id = user.getAttributes().get("id");
        if (id != null) {
            return String.valueOf(id);
        }
        return user.getName();
    }

    private String resolveEmail(OAuth2User user) {
        Object email = user.getAttributes().get("email");
        return email == null ? null : String.valueOf(email);
    }

    private boolean resolveEmailVerified(OAuth2User user) {
        Object emailVerified = user.getAttributes().get("email_verified");
        if (emailVerified instanceof Boolean value) {
            return value;
        }
        if (emailVerified instanceof String value) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }

    private String normalizeEmailOrNull(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}
