package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.user.application.UserApplicationService;
import java.time.Clock;

public final class HouseholdInMemoryWiring {
    private HouseholdInMemoryWiring() {
    }

    public static HouseholdApplicationService createApplicationService(UserApplicationService userApplicationService) {
        HouseholdPersistenceAdapter adapter = new HouseholdPersistenceAdapter();
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryInvitationTokenGenerator tokenGenerator = new InMemoryInvitationTokenGenerator();
        return HouseholdApplicationService.create(
                adapter,
                adapter,
                invitationRepository,
                tokenGenerator,
                userApplicationService,
                Clock.systemUTC()
        );
    }
}
