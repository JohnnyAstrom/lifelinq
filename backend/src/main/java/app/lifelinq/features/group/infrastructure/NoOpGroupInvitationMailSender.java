package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.GroupInvitationMailSender;
import java.time.Instant;

public final class NoOpGroupInvitationMailSender implements GroupInvitationMailSender {
    @Override
    public void sendInvitationEmail(
            String email,
            String invitePreviewUrl,
            String shortCode,
            Instant expiresAt,
            String inviterDisplayName,
            String groupName
    ) {
        // Intentionally no-op for non-persistence test and in-memory profiles.
    }
}

