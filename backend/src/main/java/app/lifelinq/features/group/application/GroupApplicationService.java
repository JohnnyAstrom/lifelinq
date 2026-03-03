package app.lifelinq.features.group.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.CreateInvitationOutput;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.LastAdminRemovalException;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationType;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.user.contract.UserProvisioning;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import app.lifelinq.features.user.contract.UserProfileView;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class GroupApplicationService {
    private static final Duration DEFAULT_INVITATION_TTL = Duration.ofDays(7);
    private static final int DEFAULT_EMAIL_INVITATION_MAX_USES = 1;
    private static final int MAX_PLACE_NAME_LENGTH = 50;
    static final String DEFAULT_GROUP_NAME = "Personal";

    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateGroupUseCase createGroupUseCase;
    private final AddMemberToGroupUseCase addMemberToGroupUseCase;
    private final ListGroupMembersUseCase listGroupMembersUseCase;
    private final RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase;
    private final CreateInvitationUseCase createInvitationUseCase;
    private final ResolveInvitationCodeUseCase resolveInvitationCodeUseCase;
    private final PreviewInvitationUseCase previewInvitationUseCase;
    private final RevokeInvitationUseCase revokeInvitationUseCase;
    private final InvitationRepository invitationRepository;
    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserProvisioning userProvisioning;
    private final UserActiveGroupRead userActiveGroupRead;
    private final UserActiveGroupSelection userActiveGroupSelection;
    private final UserProfileRead userProfileRead;
    private final Clock clock;

    public GroupApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateGroupUseCase createGroupUseCase,
            AddMemberToGroupUseCase addMemberToGroupUseCase,
            ListGroupMembersUseCase listGroupMembersUseCase,
            RemoveMemberFromGroupUseCase removeMemberFromGroupUseCase,
            CreateInvitationUseCase createInvitationUseCase,
            ResolveInvitationCodeUseCase resolveInvitationCodeUseCase,
            PreviewInvitationUseCase previewInvitationUseCase,
            RevokeInvitationUseCase revokeInvitationUseCase,
            InvitationRepository invitationRepository,
            MembershipRepository membershipRepository,
            GroupRepository groupRepository,
            UserProvisioning userProvisioning,
            UserActiveGroupRead userActiveGroupRead,
            UserActiveGroupSelection userActiveGroupSelection,
            UserProfileRead userProfileRead,
            Clock clock
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createGroupUseCase = createGroupUseCase;
        this.addMemberToGroupUseCase = addMemberToGroupUseCase;
        this.listGroupMembersUseCase = listGroupMembersUseCase;
        this.removeMemberFromGroupUseCase = removeMemberFromGroupUseCase;
        this.createInvitationUseCase = createInvitationUseCase;
        this.resolveInvitationCodeUseCase = resolveInvitationCodeUseCase;
        this.previewInvitationUseCase = previewInvitationUseCase;
        this.revokeInvitationUseCase = revokeInvitationUseCase;
        this.invitationRepository = invitationRepository;
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
        this.userProvisioning = userProvisioning;
        if (userActiveGroupRead == null) {
            throw new IllegalArgumentException("userActiveGroupRead must not be null");
        }
        this.userActiveGroupRead = userActiveGroupRead;
        this.userActiveGroupSelection = userActiveGroupSelection;
        this.userProfileRead = userProfileRead;
        this.clock = clock;
    }

    @Transactional
    public MembershipId acceptInvitation(String token, UUID userId) {
        userProvisioning.ensureUserExists(userId);
        AcceptInvitationCommand command = new AcceptInvitationCommand(token, userId, clock.instant());
        AcceptInvitationResult result = acceptInvitationUseCase.execute(command);
        userActiveGroupSelection.setActiveGroup(userId, result.getGroupId());
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
        String inviterDisplayName = actorDisplayName(actorUserId);
        Duration effectiveTtl = ttl == null ? DEFAULT_INVITATION_TTL : ttl;
        CreateInvitationCommand command = new CreateInvitationCommand(
                groupId,
                InvitationType.EMAIL,
                normalizedEmail,
                inviterDisplayName,
                clock.instant(),
                effectiveTtl,
                DEFAULT_EMAIL_INVITATION_MAX_USES
        );
        CreateInvitationResult result = createInvitationUseCase.execute(command);
        return new CreateInvitationOutput(
                result.getInvitationId(),
                result.getToken(),
                result.getExpiresAt()
        );
    }

    @Transactional
    public CreateInvitationOutput createLinkInvitation(
            UUID groupId,
            UUID actorUserId,
            Duration ttl,
            Integer maxUses
    ) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureAdmin(groupId, actorUserId);
        Duration effectiveTtl = ttl == null ? DEFAULT_INVITATION_TTL : ttl;
        CreateInvitationCommand command = new CreateInvitationCommand(
                groupId,
                InvitationType.LINK,
                null,
                null,
                clock.instant(),
                effectiveTtl,
                maxUses
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

    @Transactional(readOnly = true)
    public PreviewInvitationResult previewInvitation(String token) {
        return previewInvitationUseCase.execute(new PreviewInvitationCommand(token, clock.instant()));
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> resolveInvitationCode(String code) {
        return resolveInvitationCodeUseCase.execute(new ResolveInvitationCodeCommand(code));
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> getActiveLinkInvitation(UUID groupId, UUID actorUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureAdmin(groupId, actorUserId);
        Instant now = clock.instant();
        for (Invitation invitation : invitationRepository.findActive()) {
            if (invitation.getType() == InvitationType.LINK
                    && groupId.equals(invitation.getGroupId())
                    && invitation.isAcceptAllowed(now)) {
                return Optional.of(invitation);
            }
        }
        return Optional.empty();
    }

    @Transactional
    public UUID createGroup(String name, UUID ownerUserId) {
        userProvisioning.ensureUserExists(ownerUserId);
        CreateGroupCommand command = new CreateGroupCommand(name, ownerUserId);
        CreateGroupResult result = createGroupUseCase.execute(command);
        return result.getGroupId();
    }

    @Transactional
    public void renameCurrentPlace(UUID groupId, UUID actorUserId, String name) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureAdmin(groupId, actorUserId);
        String normalizedName = normalizePlaceName(name);
        var existing = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("group not found"));
        groupRepository.save(new app.lifelinq.features.group.domain.Group(existing.getId(), normalizedName));
    }

    @Transactional
    public void leaveCurrentPlace(UUID groupId, UUID actorUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureNotDefaultPlace(groupId, actorUserId);

        List<Membership> userMemberships = membershipRepository.findByUserId(actorUserId);
        if (userMemberships.size() <= 1) {
            throw new IllegalStateException("cannot leave your only place");
        }

        List<Membership> groupMemberships = membershipRepository.findByGroupId(groupId);
        ensureActorIsMember(groupId, actorUserId, groupMemberships);
        ensureNotBlockedLastAdminRemoval(actorUserId, groupMemberships);

        RemoveMemberFromGroupResult result = removeMemberFromGroupUseCase.execute(
                new RemoveMemberFromGroupCommand(groupId, actorUserId)
        );
        if (!result.isRemoved()) {
            throw new IllegalStateException("membership not found");
        }

        if (groupMemberships.size() == 1) {
            groupRepository.deleteById(groupId);
        }

        List<Membership> remainingMemberships = membershipRepository.findByUserId(actorUserId);
        if (!remainingMemberships.isEmpty()) {
            UUID nextGroupId = remainingMemberships.stream()
                    .map(Membership::getGroupId)
                    .sorted(Comparator.comparing(UUID::toString))
                    .findFirst()
                    .orElseThrow();
            userActiveGroupSelection.setActiveGroup(actorUserId, nextGroupId);
            return;
        }

        UUID defaultGroupId = ensureDefaultGroupProvisioned(actorUserId);
        userActiveGroupSelection.setActiveGroup(actorUserId, defaultGroupId);
    }

    @Transactional
    public void deleteCurrentPlace(UUID groupId, UUID actorUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureAdmin(groupId, actorUserId);
        ensureNotDefaultPlace(groupId, actorUserId);

        List<Membership> actorMemberships = membershipRepository.findByUserId(actorUserId);
        if (actorMemberships.size() <= 1) {
            throw new IllegalStateException("cannot delete your only place");
        }

        List<Membership> groupMemberships = membershipRepository.findByGroupId(groupId);
        List<UUID> affectedUserIds = groupMemberships.stream()
                .map(Membership::getUserId)
                .distinct()
                .toList();

        for (UUID userId : affectedUserIds) {
            UUID activeGroupId = userActiveGroupRead.getActiveGroupId(userId);
            if (groupId.equals(activeGroupId)) {
                userActiveGroupSelection.clearActiveGroup(userId);
            }
        }

        membershipRepository.deleteByGroupId(groupId);
        groupRepository.deleteById(groupId);
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
    public List<GroupMemberView> listMembers(UUID groupId) {
        ListGroupMembersResult result = listGroupMembersUseCase.execute(
                new ListGroupMembersCommand(groupId)
        );
        return result.getMembers().stream()
                .map(this::toGroupMemberView)
                .toList();
    }

    @Transactional
    public UUID ensureDefaultGroupProvisioned(UUID userId) {
        return ensureDefaultGroupProvisioned(userId, null);
    }

    @Transactional
    public UUID ensureDefaultGroupProvisioned(UUID userId, String initialPlaceName) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        UUID defaultGroupId = defaultGroupIdFor(userId);
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        for (Membership membership : memberships) {
            if (defaultGroupId.equals(membership.getGroupId())) {
                return defaultGroupId;
            }
        }
        if (!memberships.isEmpty()) {
            return memberships.stream()
                    .map(Membership::getGroupId)
                    .sorted(Comparator.comparing(UUID::toString))
                    .findFirst()
                    .orElseThrow();
        }
        String groupName = normalizeInitialPlaceName(initialPlaceName);
        groupRepository.save(new app.lifelinq.features.group.domain.Group(defaultGroupId, groupName));
        membershipRepository.save(new Membership(defaultGroupId, userId, GroupRole.ADMIN));
        return defaultGroupId;
    }

    public static GroupApplicationService create(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator,
            InvitationShortCodeGenerator shortCodeGenerator,
            UserProvisioning userProvisioning,
            UserActiveGroupRead userActiveGroupRead,
            UserActiveGroupSelection userActiveGroupSelection,
            UserProfileRead userProfileRead,
            Clock clock
    ) {
        return new GroupApplicationService(
                new AcceptInvitationUseCase(invitationRepository, membershipRepository),
                new CreateGroupUseCase(groupRepository, membershipRepository),
                new AddMemberToGroupUseCase(membershipRepository),
                new ListGroupMembersUseCase(membershipRepository),
                new RemoveMemberFromGroupUseCase(membershipRepository),
                new CreateInvitationUseCase(invitationRepository, tokenGenerator, shortCodeGenerator),
                new ResolveInvitationCodeUseCase(invitationRepository),
                new PreviewInvitationUseCase(invitationRepository, groupRepository),
                new RevokeInvitationUseCase(invitationRepository),
                invitationRepository,
                membershipRepository,
                groupRepository,
                userProvisioning,
                userActiveGroupRead,
                userActiveGroupSelection,
                userProfileRead,
                clock
        );
    }

    private GroupMemberView toGroupMemberView(Membership membership) {
        String displayName = actorDisplayName(membership.getUserId());
        return new GroupMemberView(membership.getUserId(), membership.getRole(), displayName);
    }

    private String actorDisplayName(UUID userId) {
        UserProfileView profile = userProfileRead.getProfile(userId);
        if (profile == null
                || profile.firstName() == null
                || profile.lastName() == null
                || profile.firstName().isBlank()
                || profile.lastName().isBlank()) {
            return null;
        }
        return profile.firstName() + " " + profile.lastName();
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

    private void ensureActorIsMember(UUID groupId, UUID actorUserId, List<Membership> memberships) {
        for (Membership membership : memberships) {
            if (membership.getGroupId().equals(groupId) && membership.getUserId().equals(actorUserId)) {
                return;
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

    private String normalizePlaceName(String placeName) {
        if (placeName == null || placeName.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return placeName.trim();
    }

    private String normalizeInitialPlaceName(String initialPlaceName) {
        if (initialPlaceName == null) {
            return DEFAULT_GROUP_NAME;
        }
        String normalized = initialPlaceName.trim();
        if (normalized.isEmpty()) {
            return DEFAULT_GROUP_NAME;
        }
        if (normalized.length() > MAX_PLACE_NAME_LENGTH) {
            throw new IllegalArgumentException("initialPlaceName must be at most " + MAX_PLACE_NAME_LENGTH + " characters");
        }
        return normalized;
    }

    private void ensureNotDefaultPlace(UUID groupId, UUID actorUserId) {
        if (defaultGroupIdFor(actorUserId).equals(groupId)) {
            throw new IllegalStateException("default place cannot be modified with this operation");
        }
    }

    static UUID defaultGroupIdFor(UUID userId) {
        return UUID.nameUUIDFromBytes(("personal-group:" + userId).getBytes(StandardCharsets.UTF_8));
    }

}
