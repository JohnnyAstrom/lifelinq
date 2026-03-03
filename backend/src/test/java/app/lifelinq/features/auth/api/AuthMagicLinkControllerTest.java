package app.lifelinq.features.auth.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.application.MagicLinkVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthMagicLinkControllerTest {
    private MockMvc mockMvc;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = Mockito.mock(AuthApplicationService.class);
        AuthMagicLinkController controller = new AuthMagicLinkController(
                authApplicationService,
                "mobileapp://auth/complete"
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void startReturnsNoContentForValidEmail() throws Exception {
        mockMvc.perform(post("/auth/magic/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isNoContent());

        verify(authApplicationService).startMagicLinkLogin("user@example.com");
    }

    @Test
    void startReturnsBadRequestForBlankEmail() throws Exception {
        mockMvc.perform(post("/auth/magic/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void verifyRedirectsWithFragmentTokenWhenValid() throws Exception {
        when(authApplicationService.verifyMagicLinkAndBuildRedirect("abc"))
                .thenReturn("mobileapp://auth/complete#token=jwt-value&refresh=refresh-value");

        mockMvc.perform(get("/auth/magic/verify").param("token", "abc"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "mobileapp://auth/complete#token=jwt-value&refresh=refresh-value"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    void verifyRedirectsToErrorWhenInvalid() throws Exception {
        doThrow(new MagicLinkVerificationException("invalid"))
                .when(authApplicationService).verifyMagicLinkAndBuildRedirect("invalid-token");

        mockMvc.perform(get("/auth/magic/verify").param("token", "invalid-token"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "mobileapp://auth/complete#error=invalid_or_expired"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }
}
