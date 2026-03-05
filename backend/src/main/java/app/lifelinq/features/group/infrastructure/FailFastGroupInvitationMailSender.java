package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.GroupInvitationMailSender;
import java.time.Instant;

public final class FailFastGroupInvitationMailSender implements GroupInvitationMailSender {
    @Override
    public void sendInvitationEmail(
            String email,
            String invitePreviewUrl,
            String shortCode,
            Instant expiresAt,
            String inviterDisplayName,
            String groupName
    ) {
        throw new IllegalStateException(
                "Group invitation mail sender is not configured for this profile. "
                        + "Provide a production GroupInvitationMailSender implementation."
        );
    }
}

