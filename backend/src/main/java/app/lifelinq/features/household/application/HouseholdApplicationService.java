package app.lifelinq.features.household.application;

import org.springframework.transaction.annotation.Transactional;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
import app.lifelinq.features.household.domain.MembershipRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class HouseholdApplicationService {
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;
    private final MembershipRepository membershipRepository;

    public HouseholdApplicationService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase,
            ListHouseholdMembersUseCase listHouseholdMembersUseCase,
            MembershipRepository membershipRepository
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createHouseholdUseCase = createHouseholdUseCase;
        this.addMemberToHouseholdUseCase = addMemberToHouseholdUseCase;
        this.listHouseholdMembersUseCase = listHouseholdMembersUseCase;
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

    @Transactional(readOnly = true)
    public List<Membership> listMembers(UUID householdId) {
        ListHouseholdMembersResult result = listHouseholdMembersUseCase.execute(
                new ListHouseholdMembersCommand(householdId)
        );
        return result.getMembers();
    }

    private void ensureOwner(UUID householdId, UUID actorUserId) {
        for (Membership membership : membershipRepository.findByHouseholdId(householdId)) {
            if (membership.getUserId().equals(actorUserId)) {
                if (membership.getRole() == HouseholdRole.OWNER) {
                    return;
                }
                throw new AccessDeniedException("Only owners can add members");
            }
        }
        throw new AccessDeniedException("Actor is not a member of the household");
    }
}
