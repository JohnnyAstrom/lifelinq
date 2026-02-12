package app.lifelinq.features.household.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextFilter;
import app.lifelinq.features.household.application.ResolveHouseholdForUserUseCase;
import app.lifelinq.features.household.application.AccessDeniedException;
import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.household.contract.CreateInvitationOutput;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.LastOwnerRemovalException;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
import app.lifelinq.features.household.domain.MembershipRepository;
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

class HouseholdControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private HouseholdApplicationService householdApplicationService;
    private FakeMembershipRepository membershipRepository;

    @BeforeEach
    void setUp() {
        membershipRepository = new FakeMembershipRepository();
        householdApplicationService = Mockito.mock(HouseholdApplicationService.class);
        HouseholdController controller = new HouseholdController(householdApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new HouseholdExceptionHandler())
                .addFilters(new RequestContextFilter(
                        new JwtVerifier(SECRET),
                        new ResolveHouseholdForUserUseCase(membershipRepository)
                ))
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
        String token = createToken(userId, Instant.now().plusSeconds(60));

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
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        MembershipId membershipId = new MembershipId(householdId, userId);
        when(householdApplicationService.acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId)
        ))
                .thenReturn(membershipId);

        mockMvc.perform(post("/households/invitations/accept")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invite-token\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId)
        );
    }

    @Test
    void createInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/households/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void createInvitationReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.createInvitation(
                Mockito.eq(householdId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/households/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvitationReturns409WhenDuplicateExists() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.createInvitation(
                Mockito.eq(householdId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenThrow(new IllegalStateException("duplicate"));

        mockMvc.perform(post("/households/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createInvitationSucceeds() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        UUID invitationId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        CreateInvitationOutput result = new CreateInvitationOutput(
                invitationId,
                "invite-token",
                expiresAt
        );

        when(householdApplicationService.createInvitation(
                Mockito.eq(householdId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenReturn(result);

        mockMvc.perform(post("/households/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void revokeInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(delete("/households/invitations/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void revokeInvitationReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.revokeInvitation(householdId, actorUserId, invitationId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(delete("/households/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void revokeInvitationSucceeds() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.revokeInvitation(householdId, actorUserId, invitationId))
                .thenReturn(true);

        mockMvc.perform(delete("/households/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void addMemberReturns401WhenHouseholdMissing() throws Exception {
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/household/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void addMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

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
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));
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
    void listMembersReturns401WhenHouseholdMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/household/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void listMembersReturns401WhenHouseholdAmbiguous() throws Exception {
        UUID userId = UUID.randomUUID();
        membershipRepository.withMemberships(userId, List.of(UUID.randomUUID(), UUID.randomUUID()));
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/household/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void listMembersSucceedsWhenHouseholdMatches() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.withMembership(userId, householdId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

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
    void removeMemberReturns401WhenHouseholdMissing() throws Exception {
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/household/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(householdApplicationService);
    }

    @Test
    void removeMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID householdId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

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
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

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
        membershipRepository.withMembership(actorUserId, householdId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(householdApplicationService.removeMember(householdId, actorUserId, targetUserId))
                .thenReturn(true);

        mockMvc.perform(post("/household/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isOk());

        verify(householdApplicationService).removeMember(householdId, actorUserId, targetUserId);
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

        FakeMembershipRepository withMembership(UUID userId, UUID householdId) {
            return withMemberships(userId, List.of(householdId));
        }

        FakeMembershipRepository withMemberships(UUID userId, List<UUID> householdIds) {
            byUser.put(userId, householdIds);
            return this;
        }

        @Override
        public void save(Membership membership) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            return byUser.getOrDefault(userId, List.of());
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
