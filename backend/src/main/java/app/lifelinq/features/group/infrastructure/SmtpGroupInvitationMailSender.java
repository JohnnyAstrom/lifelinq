package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.application.GroupInvitationMailSender;
import java.time.Instant;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public final class SmtpGroupInvitationMailSender implements GroupInvitationMailSender {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpGroupInvitationMailSender(JavaMailSender mailSender, String fromAddress) {
        if (mailSender == null) {
            throw new IllegalArgumentException("mailSender must not be null");
        }
        this.mailSender = mailSender;
        this.fromAddress = normalizeOrNull(fromAddress);
    }

    @Override
    public void sendInvitationEmail(
            String email,
            String invitePreviewUrl,
            String shortCode,
            Instant expiresAt,
            String inviterDisplayName,
            String groupName
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (invitePreviewUrl == null || invitePreviewUrl.isBlank()) {
            throw new IllegalArgumentException("invitePreviewUrl must not be blank");
        }

        String resolvedGroupName = normalizeOrFallback(groupName, "your LifeLinq group");
        String resolvedInviter = normalizeOrFallback(inviterDisplayName, "Someone");
        String expiryText = expiresAt == null ? "N/A" : expiresAt.toString();
        String codeText = shortCode == null || shortCode.isBlank() ? "N/A" : shortCode;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        if (fromAddress != null) {
            message.setFrom(fromAddress);
        }
        message.setSubject("You're invited to join LifeLinq");
        message.setText(
                resolvedInviter
                        + " invited you to join "
                        + resolvedGroupName
                        + ".\n\n"
                        + "Open your invitation:\n"
                        + invitePreviewUrl
                        + "\n\n"
                        + "Invite code: "
                        + codeText
                        + "\n"
                        + "Expires at: "
                        + expiryText
                        + "\n\n"
                        + "If you did not expect this invitation you can ignore this email."
        );
        mailSender.send(message);
    }

    private String normalizeOrNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeOrFallback(String value, String fallback) {
        String normalized = normalizeOrNull(value);
        return normalized == null ? fallback : normalized;
    }
}

