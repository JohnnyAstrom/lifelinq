package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthMailSender;

public final class FailFastAuthMailSender implements AuthMailSender {
    @Override
    public void sendMagicLink(String email, String verifyUrl) {
        throw new IllegalStateException(
                "Magic link mail sender is not configured for this profile. "
                        + "Provide a production AuthMailSender implementation."
        );
    }
}
