package app.lifelinq.features.group.application;

import java.time.Instant;

public interface GroupInvitationMailSender {
    void sendInvitationEmail(
            String email,
            String invitePreviewUrl,
            String shortCode,
            Instant expiresAt,
            String inviterDisplayName,
            String groupName
    );
}

