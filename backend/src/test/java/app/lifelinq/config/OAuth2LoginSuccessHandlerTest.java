package app.lifelinq.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.user.application.UserApplicationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class OAuth2LoginSuccessHandlerTest {
    private static final String SECRET = "test-secret";

    @Test
    void issuesTokenViaAuthOrchestrator() throws Exception {
        AuthApplicationService authApplicationService = Mockito.mock(AuthApplicationService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJwt = "signed.jwt.token";
        UUID expectedUserId = OAuth2LoginSuccessHandler.deterministicUserId("google", "provider-sub");
        when(authApplicationService.ensureProvisionedAndSignToken(expectedUserId)).thenReturn(expectedJwt);
        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                authApplicationService,
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

        assertEquals(expectedJwt, token);
        verify(authApplicationService).ensureProvisionedAndSignToken(expectedUserId);
    }
}
