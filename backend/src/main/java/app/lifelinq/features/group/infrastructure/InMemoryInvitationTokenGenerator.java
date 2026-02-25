package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.InvitationTokenGenerator;
import java.util.UUID;

public final class InMemoryInvitationTokenGenerator implements InvitationTokenGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
