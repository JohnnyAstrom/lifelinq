package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.LastOwnerRemovalException;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import app.lifelinq.features.household.contract.CreateInvitationOutput;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class HouseholdApplicationService {
    private static final Duration DEFAULT_INVITATION_TTL = Duration.ofDays(7);

    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;
    private final RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase;
    private final CreateInvitationUseCase createInvitationUseCase;
    private final RevokeInvitationUseCase revokeInvitationUseCase;
    private final MembershipRepository membershipRepository;
    private final UserProvisioning userProvisioning;
    private final ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase;
    private final Clock clock;

    public HouseholdApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase,
            ListHouseholdMembersUseCase listHouseholdMembersUseCase,
            RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase,
            CreateInvitationUseCase createInvitationUseCase,
            RevokeInvitationUseCase revokeInvitationUseCase,
            MembershipRepository membershipRepository,
            UserProvisioning userProvisioning,
            ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase,
            Clock clock
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createHouseholdUseCase = createHouseholdUseCase;
        this.addMemberToHouseholdUseCase = addMemberToHouseholdUseCase;
        this.listHouseholdMembersUseCase = listHouseholdMembersUseCase;
        this.removeMemberFromHouseholdUseCase = removeMemberFromHouseholdUseCase;
        this.createInvitationUseCase = createInvitationUseCase;
        this.revokeInvitationUseCase = revokeInvitationUseCase;
        this.membershipRepository = membershipRepository;
        this.userProvisioning = userProvisioning;
        this.resolveHouseholdForUserUseCase = resolveHouseholdForUserUseCase;
        this.clock = clock;
    }

    @Transactional
    public MembershipId acceptInvitation(String token, UUID userId) {
        userProvisioning.ensureUserExists(userId);
        AcceptInvitationCommand command = new AcceptInvitationCommand(token, userId, clock.instant());
        AcceptInvitationResult result = acceptInvitationUseCase.execute(command);
        return new MembershipId(result.getHouseholdId(), result.getUserId());
    }

    @Transactional
    public CreateInvitationOutput createInvitation(
            UUID householdId,
            UUID actorUserId,
            String inviteeEmail,
            Duration ttl
    ) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(householdId, actorUserId);
        String normalizedEmail = normalizeEmail(inviteeEmail);
        Duration effectiveTtl = ttl == null ? DEFAULT_INVITATION_TTL : ttl;
        CreateInvitationCommand command = new CreateInvitationCommand(
                householdId,
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
    public boolean revokeInvitation(UUID householdId, UUID actorUserId, UUID invitationId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(householdId, actorUserId);
        RevokeInvitationCommand command = new RevokeInvitationCommand(invitationId, clock.instant());
        RevokeInvitationResult result = revokeInvitationUseCase.execute(command);
        return result.isRevoked();
    }

    @Transactional
    public UUID createHousehold(String name, UUID ownerUserId) {
        userProvisioning.ensureUserExists(ownerUserId);
        CreateHouseholdCommand command = new CreateHouseholdCommand(name, ownerUserId);
        CreateHouseholdResult result = createHouseholdUseCase.execute(command);
        return result.getHouseholdId();
    }

    @Transactional
    public Membership addMember(UUID householdId, UUID actorUserId, UUID targetUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        ensureOwner(householdId, actorUserId);
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, targetUserId);
        AddMemberToHouseholdResult result = addMemberToHouseholdUseCase.execute(command);
        return new Membership(result.getHouseholdId(), result.getUserId(), result.getRole());
    }

    @Transactional
    public boolean removeMember(UUID householdId, UUID actorUserId, UUID targetUserId) {
        userProvisioning.ensureUserExists(actorUserId);
        List<Membership> memberships = membershipRepository.findByHouseholdId(householdId);
        ensureOwner(householdId, actorUserId, memberships);
        ensureNotLastOwner(targetUserId, memberships);
        RemoveMemberFromHouseholdCommand command = new RemoveMemberFromHouseholdCommand(householdId, targetUserId);
        RemoveMemberFromHouseholdResult result = removeMemberFromHouseholdUseCase.execute(command);
        return result.isRemoved();
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(UUID householdId) {
        ListHouseholdMembersResult result = listHouseholdMembersUseCase.execute(
                new ListHouseholdMembersCommand(householdId)
        );
        return result.getMembers();
    }

    // Context resolution (scoping only, no business mutation).
    public java.util.Optional<UUID> resolveHouseholdForUser(UUID userId) {
        return resolveHouseholdForUserUseCase.resolveForUser(userId);
    }

    public static HouseholdApplicationService create(
            HouseholdRepository householdRepository,
            MembershipRepository membershipRepository,
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator,
            UserProvisioning userProvisioning,
            Clock clock
    ) {
        ResolveHouseholdForUserUseCase resolveHouseholdForUserUseCase =
                new ResolveHouseholdForUserUseCase(membershipRepository);
        return new HouseholdApplicationService(
                new AcceptInvitationUseCase(invitationRepository, membershipRepository),
                new CreateHouseholdUseCase(householdRepository, membershipRepository),
                new AddMemberToHouseholdUseCase(membershipRepository),
                new ListHouseholdMembersUseCase(membershipRepository),
                new RemoveMemberFromHouseholdUseCase(membershipRepository),
                new CreateInvitationUseCase(invitationRepository, tokenGenerator),
                new RevokeInvitationUseCase(invitationRepository),
                membershipRepository,
                userProvisioning,
                resolveHouseholdForUserUseCase,
                clock
        );
    }

    private void ensureOwner(UUID householdId, UUID actorUserId) {
        ensureOwner(householdId, actorUserId, membershipRepository.findByHouseholdId(householdId));
    }

    private void ensureOwner(UUID householdId, UUID actorUserId, List<Membership> memberships) {
        for (Membership membership : memberships) {
            if (membership.getUserId().equals(actorUserId)) {
                if (membership.getRole() == HouseholdRole.OWNER) {
                    return;
                }
                throw new AccessDeniedException("Only owners can perform this action");
            }
        }
        throw new AccessDeniedException("Actor is not a member of the household");
    }

    private void ensureNotLastOwner(UUID targetUserId, List<Membership> memberships) {
        int ownerCount = 0;
        boolean targetIsOwner = false;
        for (Membership membership : memberships) {
            if (membership.getRole() == HouseholdRole.OWNER) {
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
