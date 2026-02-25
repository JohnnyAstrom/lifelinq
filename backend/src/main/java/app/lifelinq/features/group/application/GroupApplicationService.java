package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.LastOwnerRemovalException;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import app.lifelinq.features.group.contract.CreateInvitationOutput;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class GroupApplicationService {
    private static final Duration DEFAULT_INVITATION_TTL = Duration.ofDays(7);

    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateGroupUseCase createGroupUseCase;
    private final AddMemberToGroupUseCase addMemberToGroupUseCase;
    private final ListGroupMembersUseCase listGroupMembersUseCase;
    private final RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase;
    private final CreateInvitationUseCase createInvitationUseCase;
    private final RevokeInvitationUseCase revokeInvitationUseCase;
    private final MembershipRepository membershipRepository;
    private final UserProvisioning userProvisioning;
    private final ResolveGroupForUserUseCase resolveGroupForUserUseCase;
    private final Clock clock;

    public GroupApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateGroupUseCase createGroupUseCase,
            AddMemberToGroupUseCase addMemberToGroupUseCase,
            ListGroupMembersUseCase listGroupMembersUseCase,
            RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase,
            CreateInvitationUseCase createInvitationUseCase,
            RevokeInvitationUseCase revokeInvitationUseCase,
            MembershipRepository membershipRepository,
            UserProvisioning userProvisioning,
            ResolveGroupForUserUseCase resolveGroupForUserUseCase,
            Clock clock
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createGroupUseCase = createGroupUseCase;
        this.addMemberToGroupUseCase = addMemberToGroupUseCase;
        this.listGroupMembersUseCase = listGroupMembersUseCase;
        this.removeMemberFromGroupUseCase = removeMemberFromGroupUseCase;
        this.createInvitationUseCase = createInvitationUseCase;
        this.revokeInvitationUseCase = revokeInvitationUseCase;
        this.membershipRepository = membershipRepository;
        this.userProvisioning = userProvisioning;
        this.resolveGroupForUserUseCase = resolveGroupForUserUseCase;
        this.clock = clock;
    }

    @Transactional
    public MembershipId acceptInvitation(String token, UUID userId) {
        userProvisioning.ensureUserExists(userId);
        AcceptInvitationCommand command = new AcceptInvitationCommand(token, userId, clock.instant());
        AcceptInvitationResult result = acceptInvitationUseCase.execute(command);
        return new MembershipId(result.getGroupId(), result.getUserId());
    }

    @Transactional
    public CreateInvitationOutput createInvitation(
            UUID groupId,
            UUID actorUserId,
            String inviteeEmail,
            Duration ttl
    ) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(groupId, actorUserId);
        String normalizedEmail = normalizeEmail(inviteeEmail);
        Duration effectiveTtl = ttl == null ? DEFAULT_INVITATION_TTL : ttl;
        CreateInvitationCommand command = new CreateInvitationCommand(
                groupId,
                normalizedEmail,
                clock.instant(),
                effectiveTtl
        );
        CreateInvitationResult result = createInvitationUseCase.execute(command);
        return new CreateInvitationOutput(
                result.getInvitationId(),
                result.getToken(),
                result.getExpiresAt()
        );
    }

    @Transactional
    public boolean revokeInvitation(UUID groupId, UUID actorUserId, UUID invitationId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(groupId, actorUserId);
        RevokeInvitationCommand command = new RevokeInvitationCommand(invitationId, clock.instant());
        RevokeInvitationResult result = revokeInvitationUseCase.execute(command);
        return result.isRevoked();
    }

    @Transactional
    public UUID createGroup(String name, UUID ownerUserId) {
        userProvisioning.ensureUserExists(ownerUserId);
        CreateGroupCommand command = new CreateGroupCommand(name, ownerUserId);
        CreateGroupResult result = createGroupUseCase.execute(command);
        return result.getGroupId();
    }

    @Transactional
    public Membership addMember(UUID groupId, UUID actorUserId, UUID targetUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(groupId, actorUserId);
        AddMemberToGroupCommand command = new AddMemberToGroupCommand(groupId, targetUserId);
        AddMemberToGroupResult result = addMemberToGroupUseCase.execute(command);
        return new Membership(result.getGroupId(), result.getUserId(), result.getRole());
    }

    @Transactional
    public boolean removeMember(UUID groupId, UUID actorUserId, UUID targetUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);
        ensureOwner(groupId, actorUserId, memberships);
        ensureNotLastOwner(targetUserId, memberships);
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(groupId, targetUserId);
        RemoveMemberFromGroupResult result = removeMemberFromGroupUseCase.execute(command);
        return result.isRemoved();
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(UUID groupId) {
        ListGroupMembersResult result = listGroupMembersUseCase.execute(
                new ListGroupMembersCommand(groupId)
        );
        return result.getMembers();
    }

    // Context resolution (scoping only, no business mutation).
    public java.util.Optional<UUID> resolveGroupForUser(UUID userId) {
        return resolveGroupForUserUseCase.resolveForUser(userId);
    }

    public static GroupApplicationService create(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator,
            UserProvisioning userProvisioning,
            Clock clock
    ) {
        ResolveGroupForUserUseCase resolveGroupForUserUseCase =
                new ResolveGroupForUserUseCase(membershipRepository);
        return new GroupApplicationService(
                new AcceptInvitationUseCase(invitationRepository, membershipRepository),
                new CreateGroupUseCase(groupRepository, membershipRepository),
                new AddMemberToGroupUseCase(membershipRepository),
                new ListGroupMembersUseCase(membershipRepository),
                new RemoveMemberFromGroupUseCase(membershipRepository),
                new CreateInvitationUseCase(invitationRepository, tokenGenerator),
                new RevokeInvitationUseCase(invitationRepository),
                membershipRepository,
                userProvisioning,
                resolveGroupForUserUseCase,
                clock
        );
    }

    private void ensureOwner(UUID groupId, UUID actorUserId) {
        ensureOwner(groupId, actorUserId, membershipRepository.findByGroupId(groupId));
    }

    private void ensureOwner(UUID groupId, UUID actorUserId, List<Membership> memberships) {
        for (Membership membership : memberships) {
            if (membership.getUserId().equals(actorUserId)) {
                if (membership.getRole() == GroupRole.OWNER) {
                    return;
                }
                throw new AccessDeniedException("Only owners can perform this action");
            }
        }
        throw new AccessDeniedException("Actor is not a member of the group");
    }

    private void ensureNotLastOwner(UUID targetUserId, List<Membership> memberships) {
        int ownerCount = 0;
        boolean targetIsOwner = false;
        for (Membership membership : memberships) {
            if (membership.getRole() == GroupRole.OWNER) {
                ownerCount++;
                if (membership.getUserId().equals(targetUserId)) {
                    targetIsOwner = true;
                }
            }
        }
        if (targetIsOwner && ownerCount <= 1) {
            throw new LastOwnerRemovalException("Cannot remove the last owner");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

}
