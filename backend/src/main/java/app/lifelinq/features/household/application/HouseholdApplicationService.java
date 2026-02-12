package app.lifelinq.features.household.application;

import org.springframework.transaction.annotation.Transactional;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.LastOwnerRemovalException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class HouseholdApplicationService {
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;
    private final RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase;
    private final MembershipRepository membershipRepository;

    public HouseholdApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase,
            ListHouseholdMembersUseCase listHouseholdMembersUseCase,
            RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase,
            MembershipRepository membershipRepository
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createHouseholdUseCase = createHouseholdUseCase;
        this.addMemberToHouseholdUseCase = addMemberToHouseholdUseCase;
        this.listHouseholdMembersUseCase = listHouseholdMembersUseCase;
        this.removeMemberFromHouseholdUseCase = removeMemberFromHouseholdUseCase;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public MembershipId acceptInvitation(String token, UUID userId, Instant now) {
        AcceptInvitationCommand command = new AcceptInvitationCommand(token, userId, now);
        AcceptInvitationResult result = acceptInvitationUseCase.execute(command);
        return new MembershipId(result.getHouseholdId(), result.getUserId());
    }

    @Transactional
    public UUID createHousehold(String name, UUID ownerUserId) {
        CreateHouseholdCommand command = new CreateHouseholdCommand(name, ownerUserId);
        CreateHouseholdResult result = createHouseholdUseCase.execute(command);
        return result.getHouseholdId();
    }

    @Transactional
    public Membership addMember(UUID householdId, UUID actorUserId, UUID targetUserId) {
        ensureOwner(householdId, actorUserId);
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, targetUserId);
        AddMemberToHouseholdResult result = addMemberToHouseholdUseCase.execute(command);
        return new Membership(result.getHouseholdId(), result.getUserId(), result.getRole());
    }

    @Transactional
    public boolean removeMember(UUID householdId, UUID actorUserId, UUID targetUserId) {
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
}
