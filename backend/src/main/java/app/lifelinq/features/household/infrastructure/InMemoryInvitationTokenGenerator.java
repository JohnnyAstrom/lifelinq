package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.application.InvitationTokenGenerator;
import java.util.UUID;

public final class InMemoryInvitationTokenGenerator implements InvitationTokenGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
