package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DevAuthMailSender implements AuthMailSender {
    private static final Logger log = LoggerFactory.getLogger(DevAuthMailSender.class);

    @Override
    public void sendMagicLink(String email, String verifyUrl) {
        log.info("Magic link login requested for {}", email);
    }
}

