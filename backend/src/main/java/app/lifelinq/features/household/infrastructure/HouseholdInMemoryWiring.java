package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;

public final class HouseholdInMemoryWiring {
    private HouseholdInMemoryWiring() {
    }

    public static HouseholdApplicationService createApplicationService(UserProvisioning userProvisioning) {
        HouseholdPersistenceAdapter adapter = new HouseholdPersistenceAdapter();
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryInvitationTokenGenerator tokenGenerator = new InMemoryInvitationTokenGenerator();
        return HouseholdApplicationService.create(
                adapter,
                adapter,
                invitationRepository,
                tokenGenerator,
                userProvisioning,
                Clock.systemUTC()
        );
    }
}
