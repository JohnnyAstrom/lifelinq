package app.lifelinq.features.group.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.lifelinq.config.AuthenticationFilter;
import app.lifelinq.config.GroupContextFilter;
import app.lifelinq.config.JwtVerifier;
import app.lifelinq.config.RequestContextExceptionHandler;
import app.lifelinq.test.FakeActiveGroupUserRepository;
import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.application.AdminRemovalConflictException;
import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.application.GroupMemberView;
import app.lifelinq.features.group.contract.CreateInvitationOutput;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.LastAdminRemovalException;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GroupControllerTest {
    private static final String SECRET = "test-secret";

    private MockMvc mockMvc;
    private GroupApplicationService groupApplicationService;
    private FakeActiveGroupUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new FakeActiveGroupUserRepository();
        groupApplicationService = Mockito.mock(GroupApplicationService.class);
        GroupController controller = new GroupController(groupApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GroupExceptionHandler(), new RequestContextExceptionHandler())
                .addFilters(
                        new AuthenticationFilter(new JwtVerifier(SECRET)),
                        new GroupContextFilter(userRepository)
                )
                .build();
    }

    @Test
    void addMemberReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void listMembersReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/groups/members"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void createReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Home\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void createSucceedsWithUserIdFromToken() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(groupApplicationService.createGroup("Home", userId)).thenReturn(groupId);

        mockMvc.perform(post("/groups")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Home\"}"))
                .andExpect(status().isOk());

        verify(groupApplicationService).createGroup("Home", userId);
    }

    @Test
    void acceptInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invite-token\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void acceptInvitationSucceedsWithUserIdFromToken() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        MembershipId membershipId = new MembershipId(groupId, userId);
        when(groupApplicationService.acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId)
        ))
                .thenReturn(membershipId);

        mockMvc.perform(post("/groups/invitations/accept")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invite-token\"}"))
                .andExpect(status().isOk());

        verify(groupApplicationService).acceptInvitation(
                Mockito.eq("invite-token"),
                Mockito.eq(userId)
        );
    }

    @Test
    void resolveInvitationCodeReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/invitations/resolve-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"K7M9XQ\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void resolveInvitationCodeReturns404WhenCodeNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(groupApplicationService.resolveInvitationCode("K7M9XQ"))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/groups/invitations/resolve-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"K7M9XQ\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolveInvitationCodeSucceeds() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        Invitation result = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                app.lifelinq.features.group.domain.InvitationType.LINK,
                null,
                null,
                "token-1",
                "K7M9XQ",
                Instant.now().plusSeconds(3600),
                null
        );

        when(groupApplicationService.resolveInvitationCode("K7M9XQ"))
                .thenReturn(java.util.Optional.of(result));

        mockMvc.perform(post("/groups/invitations/resolve-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"K7M9XQ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationId").value(result.getId().toString()))
                .andExpect(jsonPath("$.groupId").value(result.getGroupId().toString()))
                .andExpect(jsonPath("$.token").value(result.getToken()))
                .andExpect(jsonPath("$.type").value("LINK"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void createInvitationReturns403WhenActorIsNotOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.createInvitation(
                Mockito.eq(groupId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/groups/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInvitationReturns409WhenDuplicateExists() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.createInvitation(
                Mockito.eq(groupId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenThrow(new IllegalStateException("duplicate"));

        mockMvc.perform(post("/groups/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createInvitationSucceeds() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        UUID invitationId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        CreateInvitationOutput result = new CreateInvitationOutput(
                invitationId,
                "invite-token",
                "ABC123",
                expiresAt
        );

        when(groupApplicationService.createInvitation(
                Mockito.eq(groupId),
                Mockito.eq(actorUserId),
                Mockito.eq("test@example.com"),
                Mockito.isNull()
        )).thenReturn(result);

        mockMvc.perform(post("/groups/invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createLinkInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/invitations/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void createLinkInvitationSucceeds() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        UUID invitationId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        CreateInvitationOutput result = new CreateInvitationOutput(
                invitationId,
                "invite-link-token",
                "K7M9XQ",
                expiresAt
        );

        when(groupApplicationService.createLinkInvitation(
                Mockito.eq(groupId),
                Mockito.eq(actorUserId),
                Mockito.isNull(),
                Mockito.isNull()
        )).thenReturn(result);

        mockMvc.perform(post("/groups/invitations/link")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("invite-link-token"))
                .andExpect(jsonPath("$.shortCode").value("K7M9XQ"));
    }

    @Test
    void createLinkInvitationReturns409WhenActiveLinkAlreadyExists() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.createLinkInvitation(
                Mockito.eq(groupId),
                Mockito.eq(actorUserId),
                Mockito.isNull(),
                Mockito.isNull()
        )).thenThrow(new IllegalStateException("active link invitation already exists"));

        mockMvc.perform(post("/groups/invitations/link")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getActiveInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(get("/groups/" + UUID.randomUUID() + "/invitations/active"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void getActiveInvitationReturns409WhenGroupIdDoesNotMatchContext() throws Exception {
        UUID activeGroupId = UUID.randomUUID();
        UUID requestedGroupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, activeGroupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/groups/" + requestedGroupId + "/invitations/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void getActiveInvitationReturns200WithEmptyPayloadWhenNoneExists() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.getActiveLinkInvitation(groupId, actorUserId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/groups/" + groupId + "/invitations/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.shortCode").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.expiresAt").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void getActiveInvitationReturns200WithInvitationWhenPresent() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                groupId,
                app.lifelinq.features.group.domain.InvitationType.LINK,
                null,
                null,
                "invite-link-token",
                "K7M9XQ",
                Instant.now().plusSeconds(3600),
                null
        );

        when(groupApplicationService.getActiveLinkInvitation(groupId, actorUserId))
                .thenReturn(Optional.of(invitation));

        mockMvc.perform(get("/groups/" + groupId + "/invitations/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitationId").value(invitation.getId().toString()))
                .andExpect(jsonPath("$.token").value("invite-link-token"))
                .andExpect(jsonPath("$.shortCode").value("K7M9XQ"));
    }

    @Test
    void revokeInvitationReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(delete("/groups/invitations/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void revokeInvitationReturns403WhenActorIsNotOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.revokeInvitation(groupId, actorUserId, invitationId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(delete("/groups/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void revokeInvitationSucceeds() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.revokeInvitation(groupId, actorUserId, invitationId))
                .thenReturn(true);

        mockMvc.perform(delete("/groups/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void revokeInvitationReturns404WhenNotFound() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.revokeInvitation(groupId, actorUserId, invitationId))
                .thenReturn(false);

        mockMvc.perform(delete("/groups/invitations/" + invitationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMemberReturns409WhenNoActiveGroupSelected() throws Exception {
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/groups/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isConflict());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void addMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.addMember(groupId, actorUserId, targetUserId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/groups/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isForbidden());

        verify(groupApplicationService).addMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void addMemberSucceedsWhenActorIsOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));
        Membership membership = new Membership(groupId, targetUserId, GroupRole.MEMBER);

        when(groupApplicationService.addMember(groupId, actorUserId, targetUserId))
                .thenReturn(membership);

        mockMvc.perform(post("/groups/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        verify(groupApplicationService).addMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void listMembersReturns409WhenNoActiveGroupSelected() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(get("/groups/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void listMembersSucceedsWhenGroupMatches() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(groupApplicationService.listMembers(groupId)).thenReturn(List.of(
                new GroupMemberView(userId, GroupRole.ADMIN, "Alice Admin")
        ));

        mockMvc.perform(get("/groups/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.members[0].displayName").value("Alice Admin"));

        verify(groupApplicationService).listMembers(groupId);
    }

    @Test
    void listMembersReturnsNullDisplayNameWhenProfileMissing() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        when(groupApplicationService.listMembers(groupId)).thenReturn(List.of(
                new GroupMemberView(userId, GroupRole.MEMBER, null)
        ));

        mockMvc.perform(get("/groups/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].role").value("MEMBER"))
                .andExpect(jsonPath("$.members[0].displayName").value(org.hamcrest.Matchers.nullValue()));

        verify(groupApplicationService).listMembers(groupId);
    }

    @Test
    void removeMemberReturns401WhenContextMissing() throws Exception {
        mockMvc.perform(post("/groups/members/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void removeMemberReturns409WhenNoActiveGroupSelected() throws Exception {
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/groups/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isConflict());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void removeMemberReturns403WhenActorIsNotOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.removeMember(groupId, actorUserId, targetUserId))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(post("/groups/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isForbidden());

        verify(groupApplicationService).removeMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void removeMemberReturns409WhenRemovingLastAdmin() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.removeMember(groupId, actorUserId, targetUserId))
                .thenThrow(new LastAdminRemovalException("last admin"));

        mockMvc.perform(post("/groups/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isConflict());

        verify(groupApplicationService).removeMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void removeMemberReturns409WhenRemovingAnotherAdmin() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.removeMember(groupId, actorUserId, targetUserId))
                .thenThrow(new AdminRemovalConflictException("Admins cannot remove other admins"));

        mockMvc.perform(post("/groups/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isConflict());

        verify(groupApplicationService).removeMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void removeMemberSucceedsWhenActorIsOwner() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        userRepository.withUser(actorUserId, groupId);
        String token = createToken(actorUserId, Instant.now().plusSeconds(60));

        when(groupApplicationService.removeMember(groupId, actorUserId, targetUserId))
                .thenReturn(true);

        mockMvc.perform(post("/groups/members/remove")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + targetUserId + "\"}"))
                .andExpect(status().isOk());

        verify(groupApplicationService).removeMember(groupId, actorUserId, targetUserId);
    }

    @Test
    void renameCurrentPlaceReturns409WhenNoActiveGroupSelected() throws Exception {
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(patch("/me/place")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Family\"}"))
                .andExpect(status().isConflict());

        verifyNoInteractions(groupApplicationService);
    }

    @Test
    void renameCurrentPlaceSucceedsWhenContextAvailable() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(patch("/me/place")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Family\"}"))
                .andExpect(status().isNoContent());

        verify(groupApplicationService).renameCurrentPlace(groupId, userId, "Family");
    }

    @Test
    void leaveCurrentPlaceSucceedsWhenContextAvailable() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(post("/me/place/leave")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(groupApplicationService).leaveCurrentPlace(groupId, userId);
    }

    @Test
    void deleteCurrentPlaceSucceedsWhenContextAvailable() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        userRepository.withUser(userId, groupId);
        String token = createToken(userId, Instant.now().plusSeconds(60));

        mockMvc.perform(delete("/me/place")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(groupApplicationService).deleteCurrentPlace(groupId, userId);
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

