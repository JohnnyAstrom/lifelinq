package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.LastAdminRemovalException;
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
        ensureAdmin(groupId, actorUserId);
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
        ensureAdmin(groupId, actorUserId);
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
        ensureAdmin(groupId, actorUserId);
        AddMemberToGroupCommand command = new AddMemberToGroupCommand(groupId, targetUserId);
        AddMemberToGroupResult result = addMemberToGroupUseCase.execute(command);
        return new Membership(result.getGroupId(), result.getUserId(), result.getRole());
    }

    @Transactional
    public boolean removeMember(UUID groupId, UUID actorUserId, UUID targetUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);
        ensureAdmin(groupId, actorUserId, memberships);
        ensureNoAdminToAdminRemoval(actorUserId, targetUserId, memberships);
        ensureNotBlockedLastAdminRemoval(targetUserId, memberships);
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

    private void ensureAdmin(UUID groupId, UUID actorUserId) {
        ensureAdmin(groupId, actorUserId, membershipRepository.findByGroupId(groupId));
    }

    private void ensureAdmin(UUID groupId, UUID actorUserId, List<Membership> memberships) {
        for (Membership membership : memberships) {
            if (membership.getUserId().equals(actorUserId)) {
                if (membership.getRole() == GroupRole.ADMIN) {
                    return;
                }
                throw new AccessDeniedException("Only admins can perform this action");
            }
        }
        throw new AccessDeniedException("Actor is not a member of the group");
    }

    private void ensureNoAdminToAdminRemoval(
            UUID actorUserId,
            UUID targetUserId,
            List<Membership> memberships
    ) {
        if (actorUserId.equals(targetUserId)) {
            return;
        }
        boolean actorIsAdmin = false;
        boolean targetIsAdmin = false;
        for (Membership membership : memberships) {
            if (membership.getRole() != GroupRole.ADMIN) {
                continue;
            }
            if (membership.getUserId().equals(actorUserId)) {
                actorIsAdmin = true;
            }
            if (membership.getUserId().equals(targetUserId)) {
                targetIsAdmin = true;
            }
        }
        if (actorIsAdmin && targetIsAdmin) {
            throw new AdminRemovalConflictException("Admins cannot remove other admins");
        }
    }

    private void ensureNotBlockedLastAdminRemoval(UUID targetUserId, List<Membership> memberships) {
        int memberCount = memberships.size();
        int adminCount = 0;
        boolean targetIsAdmin = false;
        for (Membership membership : memberships) {
            if (membership.getRole() == GroupRole.ADMIN) {
                adminCount++;
                if (membership.getUserId().equals(targetUserId)) {
                    targetIsAdmin = true;
                }
            }
        }
        if (targetIsAdmin && adminCount <= 1 && memberCount > 1) {
            throw new LastAdminRemovalException("Cannot remove the last admin while other members exist");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

}
