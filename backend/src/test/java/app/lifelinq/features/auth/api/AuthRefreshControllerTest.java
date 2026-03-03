package app.lifelinq.features.auth.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.application.AuthTokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthRefreshControllerTest {
    private MockMvc mockMvc;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = Mockito.mock(AuthApplicationService.class);
        AuthRefreshController controller = new AuthRefreshController(authApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void refreshReturnsAuthPairForValidRefreshToken() throws Exception {
        when(authApplicationService.refreshAuthTokens("refresh-token"))
                .thenReturn(new AuthTokenPair("access-jwt", "rotated-refresh"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("rotated-refresh"));

        verify(authApplicationService).refreshAuthTokens("refresh-token");
    }

    @Test
    void refreshReturnsBadRequestForBlankRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authApplicationService);
    }
}

