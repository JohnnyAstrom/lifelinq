package app.lifelinq.features.household.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
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

class HouseholdControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private HouseholdApplicationService householdApplicationService;

    @BeforeEach
    void setUp() {
        householdApplicationService = Mockito.mock(HouseholdApplicationService.class);
        HouseholdController controller = new HouseholdController(householdApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestContextFilter(new JwtVerifier(SECRET)))
                .build();
    }

    @Test
    void addMemberReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/households/{id}/members", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void listMembersReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/households/{id}/members", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void addMemberReturns403OnHouseholdMismatch() throws Exception {
        UUID pathHouseholdId = UUID.randomUUID();
        UUID tokenHouseholdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = createToken(tokenHouseholdId, UUID.randomUUID(), Instant.now().plusSeconds(60));

        mockMvc.perform(post("/households/{id}/members", pathHouseholdId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + userId + "\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void listMembersReturns403OnHouseholdMismatch() throws Exception {
        UUID pathHouseholdId = UUID.randomUUID();
        UUID tokenHouseholdId = UUID.randomUUID();
        String token = createToken(tokenHouseholdId, UUID.randomUUID(), Instant.now().plusSeconds(60));

        mockMvc.perform(get("/households/{id}/members", pathHouseholdId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void addMemberSucceedsWhenHouseholdMatches() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = createToken(householdId, UUID.randomUUID(), Instant.now().plusSeconds(60));
        Membership membership = new Membership(householdId, userId, HouseholdRole.MEMBER);

        when(householdApplicationService.addMember(householdId, userId)).thenReturn(membership);

        mockMvc.perform(post("/households/{id}/members", householdId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + userId + "\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).addMember(householdId, userId);
    }

    @Test
    void listMembersSucceedsWhenHouseholdMatches() throws Exception {
        UUID householdId = UUID.randomUUID();
        String token = createToken(householdId, UUID.randomUUID(), Instant.now().plusSeconds(60));

        when(householdApplicationService.listMembers(householdId)).thenReturn(List.of());

        mockMvc.perform(get("/households/{id}/members", householdId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(householdApplicationService).listMembers(householdId);
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
