package app.lifelinq.features.household.application;

import org.springframework.transaction.annotation.Transactional;

public class HouseholdTransactionalService {
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;

    public HouseholdTransactionalService(
            AcceptInvitationUseCase acceptInvitationUseCase,
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase
    ) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.createHouseholdUseCase = createHouseholdUseCase;
        this.addMemberToHouseholdUseCase = addMemberToHouseholdUseCase;
    }

    @Transactional
    public AcceptInvitationResult acceptInvitation(AcceptInvitationCommand command) {
        return acceptInvitationUseCase.execute(command);
    }

    @Transactional
    public CreateHouseholdResult createHousehold(CreateHouseholdCommand command) {
        return createHouseholdUseCase.execute(command);
    }

    @Transactional
    public AddMemberToHouseholdResult addMember(AddMemberToHouseholdCommand command) {
        return addMemberToHouseholdUseCase.execute(command);
    }
}
