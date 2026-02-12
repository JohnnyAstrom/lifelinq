package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.application.HouseholdUseCases;

public final class HouseholdInMemoryWiring {
    private HouseholdInMemoryWiring() {
    }

    public static HouseholdUseCases createUseCases() {
        HouseholdPersistenceAdapter adapter = new HouseholdPersistenceAdapter();
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryInvitationTokenGenerator tokenGenerator = new InMemoryInvitationTokenGenerator();
        return new HouseholdUseCases(adapter, adapter, invitationRepository, tokenGenerator);
    }
}
