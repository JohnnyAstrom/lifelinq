package app.lifelinq.features.auth.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.AuthenticationFilter;
import app.lifelinq.config.GroupContextFilter;
import app.lifelinq.config.JwtVerifier;
import app.lifelinq.features.auth.application.ActiveGroupSelectionConflictException;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.auth.contract.UserContextView;
import app.lifelinq.features.auth.contract.UserMembershipView;
import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import app.lifelinq.test.FakeActiveGroupUserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MeControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private FakeActiveGroupUserRepository userRepository;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeActiveGroupUserRepository();
        authApplicationService = Mockito.mock(AuthApplicationService.class);
        MeController controller = new MeController(authApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AuthExceptionHandler())
                .addFilters(
                        new AuthenticationFilter(new JwtVerifier(SECRET)),
                        new GroupContextFilter(userRepository)
                )
                .build();
    }

    @Test
    void returns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void returnsUserAndGroupWhenContextPresent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        when(authApplicationService.getMe(userId)).thenReturn(new UserContextView(
                userId,
                groupId,
                List.of(new UserMembershipView(groupId, "ADMIN"))
        ));

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.activeGroupId").value(groupId.toString()))
                .andExpect(jsonPath("$.memberships[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.memberships[0].role").value("ADMIN"));

        verify(authApplicationService).getMe(userId);
    }

    @Test
    void returnsNullGroupWhenMissingMembership() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        when(authApplicationService.getMe(userId)).thenReturn(new UserContextView(
                userId,
                null,
                List.of()
        ));

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.activeGroupId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.memberships").isArray());
    }

    @Test
    void returnsMembershipsWithNullActiveGroupWhenNoActiveGroupSelected() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        when(authApplicationService.getMe(userId)).thenReturn(new UserContextView(
                userId,
                null,
                List.of(new UserMembershipView(groupId, "MEMBER"))
        ));

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.activeGroupId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.memberships.length()").value(1))
                .andExpect(jsonPath("$.memberships[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.memberships[0].role").value("MEMBER"));
    }

    @Test
    void deleteMeReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(delete("/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void deleteMeReturns204OnSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(delete("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(authApplicationService).deleteAccount(userId);
    }

    @Test
    void deleteMeReturns409WhenDeletionBlockedByGroupGovernance() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        doThrow(new DeleteAccountBlockedException(
                "Account deletion blocked: you are the sole admin in one or more groups"
        )).when(authApplicationService).deleteAccount(Mockito.eq(userId));

        mockMvc.perform(delete("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        verify(authApplicationService).deleteAccount(userId);
    }

    @Test
    void setActiveGroupReturns200WithUpdatedMeResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID currentGroupId = UUID.randomUUID();
        UUID selectedGroupId = UUID.randomUUID();
        userRepository.withUser(userId, currentGroupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        when(authApplicationService.setActiveGroup(Mockito.eq(userId), Mockito.eq(selectedGroupId)))
                .thenReturn(new UserContextView(
                        userId,
                        selectedGroupId,
                        List.of(
                                new UserMembershipView(currentGroupId, "MEMBER"),
                                new UserMembershipView(selectedGroupId, "ADMIN")
                        )
                ));

        mockMvc.perform(put("/me/active-group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activeGroupId\":\"" + selectedGroupId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.activeGroupId").value(selectedGroupId.toString()))
                .andExpect(jsonPath("$.memberships.length()").value(2));
    }

    @Test
    void setActiveGroupReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(put("/me/active-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activeGroupId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setActiveGroupReturns409WhenSelectionConflicts() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID currentGroupId = UUID.randomUUID();
        UUID selectedGroupId = UUID.randomUUID();
        userRepository.withUser(userId, currentGroupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        doThrow(new ActiveGroupSelectionConflictException("Selected group is not a membership of the current user"))
                .when(authApplicationService)
                .setActiveGroup(Mockito.eq(userId), Mockito.eq(selectedGroupId));

        mockMvc.perform(put("/me/active-group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activeGroupId\":\"" + selectedGroupId + "\"}"))
                .andExpect(status().isConflict());
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

}
