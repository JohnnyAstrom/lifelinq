package app.lifelinq.features.auth.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.auth.application.AuthApplicationService;
import app.lifelinq.features.group.application.GroupApplicationServiceTestFactory;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private FakeMembershipRepository membershipRepository;
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        membershipRepository = new FakeMembershipRepository();
        authApplicationService = Mockito.mock(AuthApplicationService.class);
        MeController controller = new MeController(authApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AuthExceptionHandler())
                .addFilters(new RequestContextFilter(
                        new JwtVerifier(SECRET),
                        GroupApplicationServiceTestFactory.createForContextResolution(membershipRepository)
                ))
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
        membershipRepository.withMembership(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    }

    @Test
    void returnsNullGroupWhenMissingMembership() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.groupId").value(org.hamcrest.Matchers.nullValue()));
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
        UUID groupId = UUID.randomUUID();
        membershipRepository.withMembership(userId, groupId);
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
        membershipRepository.withMembership(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));
        doThrow(new DeleteAccountBlockedException(
                "Account deletion blocked: you are the sole admin in one or more groups"
        )).when(authApplicationService).deleteAccount(Mockito.eq(userId));

        mockMvc.perform(delete("/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        verify(authApplicationService).deleteAccount(userId);
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

        FakeMembershipRepository withMembership(UUID userId, UUID groupId) {
            byUser.put(userId, List.of(groupId));
            return this;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByGroupId(UUID groupId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByUserId(UUID userId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            return byUser.getOrDefault(userId, List.of());
        }

        @Override
        public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void deleteByUserId(UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
