package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public final class SmtpAuthMailSender implements AuthMailSender {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpAuthMailSender(JavaMailSender mailSender, String fromAddress) {
        if (mailSender == null) {
            throw new IllegalArgumentException("mailSender must not be null");
        }
        this.mailSender = mailSender;
        this.fromAddress = normalizeOrNull(fromAddress);
    }

    @Override
    public void sendMagicLink(String email, String verifyUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (verifyUrl == null || verifyUrl.isBlank()) {
            throw new IllegalArgumentException("verifyUrl must not be blank");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        if (fromAddress != null) {
            message.setFrom(fromAddress);
        }
        message.setSubject("Sign in to LifeLinq");
        message.setText(
                "Sign in to LifeLinq by clicking the link below:\n\n"
                        + verifyUrl
                        + "\n\n"
                        + "If you did not request this login you can ignore this email."
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
}

