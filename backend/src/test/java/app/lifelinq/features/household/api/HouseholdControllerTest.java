package app.lifelinq.features.household.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.household.application.AccessDeniedException;
import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.LastOwnerRemovalException;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
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
        mockMvc.perform(post("/household/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void listMembersReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/household/members"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void createReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/households")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Home\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void createSucceedsWithUserIdFromToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = createToken(householdId, userId, Instant.now().plusSeconds(60));

        when(householdApplicationService.createHousehold("Home", userId)).thenReturn(householdId);

        mockMvc.perform(post("/households")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Home\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).createHousehold("Home", userId);
    }

    @Test
    void acceptInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/households/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invite-token\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void acceptInvitationSucceedsWithUserIdFromToken() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String token = createToken(householdId, userId, Instant.now().plusSeconds(60));

        MembershipId membershipId = new MembershipId(householdId, userId);
        when(householdApplicationService.acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId),
                Mockito.any(Instant.class)
        ))
                .thenReturn(membershipId);

        mockMvc.perform(post("/households/invitations/accept")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invite-token\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId),
                Mockito.any(Instant.class)
        );
    }

    @Test
    void addMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(householdId, actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.addMember(householdId, actorUserId, targetUserId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/household/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isForbidden());

        verify(householdApplicationService).addMember(householdId, actorUserId, targetUserId);
    }

    @Test
    void addMemberSucceedsWhenActorIsOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(householdId, actorUserId, Instant.now().plusSeconds(60));
        Membership membership = new Membership(householdId, targetUserId, HouseholdRole.MEMBER);

        when(householdApplicationService.addMember(householdId, actorUserId, targetUserId))
                .thenReturn(membership);

        mockMvc.perform(post("/household/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).addMember(householdId, actorUserId, targetUserId);
    }

    @Test
    void listMembersSucceedsWhenHouseholdMatches() throws Exception {
        UUID householdId = UUID.randomUUID();
        String token = createToken(householdId, UUID.randomUUID(), Instant.now().plusSeconds(60));

        when(householdApplicationService.listMembers(householdId)).thenReturn(List.of());

        mockMvc.perform(get("/household/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(householdApplicationService).listMembers(householdId);
    }

    @Test
    void removeMemberReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/household/members/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void removeMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(householdId, actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.removeMember(householdId, actorUserId, targetUserId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/household/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isForbidden());

        verify(householdApplicationService).removeMember(householdId, actorUserId, targetUserId);
    }

    @Test
    void removeMemberReturns409WhenRemovingLastOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(householdId, actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.removeMember(householdId, actorUserId, targetUserId))
                .thenThrow(new LastOwnerRemovalException("last owner"));

        mockMvc.perform(post("/household/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isConflict());

        verify(householdApplicationService).removeMember(householdId, actorUserId, targetUserId);
    }

    @Test
    void removeMemberSucceedsWhenActorIsOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(householdId, actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.removeMember(householdId, actorUserId, targetUserId))
                .thenReturn(true);

        mockMvc.perform(post("/household/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).removeMember(householdId, actorUserId, targetUserId);
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
