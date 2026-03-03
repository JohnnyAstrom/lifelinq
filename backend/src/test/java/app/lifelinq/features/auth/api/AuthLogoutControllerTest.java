package app.lifelinq.features.auth.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.features.auth.application.AuthApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthLogoutControllerTest {
    private MockMvc mockMvc;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = Mockito.mock(AuthApplicationService.class);
        AuthLogoutController controller = new AuthLogoutController(authApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void logoutRevokesRefreshSessionForValidRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isNoContent());

        verify(authApplicationService).logoutRefreshSession("refresh-token");
    }

    @Test
    void logoutReturnsBadRequestForMissingRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authApplicationService);
    }
}

