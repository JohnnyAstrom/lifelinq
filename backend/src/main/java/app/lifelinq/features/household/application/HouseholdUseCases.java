package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.MembershipRepository;

public final class HouseholdUseCases {
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;
    private final RemoveMemberFromHouseholdUseCase removeMemberFromHouseholdUseCase;
    private final CreateInvitationUseCase createInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public HouseholdUseCases(
            HouseholdRepository householdRepository,
            MembershipRepository membershipRepository,
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator
    ) {
        if (householdRepository == null) {
            throw new IllegalArgumentException("householdRepository must not be null");
        }
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (tokenGenerator == null) {
            throw new IllegalArgumentException("tokenGenerator must not be null");
        }
        this.createHouseholdUseCase = new CreateHouseholdUseCase(householdRepository, membershipRepository);
        this.addMemberToHouseholdUseCase = new AddMemberToHouseholdUseCase(membershipRepository);
        this.listHouseholdMembersUseCase = new ListHouseholdMembersUseCase(membershipRepository);
        this.removeMemberFromHouseholdUseCase = new RemoveMemberFromHouseholdUseCase(membershipRepository);
        this.createInvitationUseCase = new CreateInvitationUseCase(invitationRepository, tokenGenerator);
        this.acceptInvitationUseCase = new AcceptInvitationUseCase(invitationRepository, membershipRepository);
    }

    public CreateHouseholdUseCase createHousehold() {
        return createHouseholdUseCase;
    }

    public AddMemberToHouseholdUseCase addMember() {
        return addMemberToHouseholdUseCase;
    }

    public ListHouseholdMembersUseCase listMembers() {
        return listHouseholdMembersUseCase;
    }

    public RemoveMemberFromHouseholdUseCase removeMember() {
        return removeMemberFromHouseholdUseCase;
    }

    public CreateInvitationUseCase createInvitation() {
        return createInvitationUseCase;
    }

    public AcceptInvitationUseCase acceptInvitation() {
        return acceptInvitationUseCase;
    }
}
