package app.lifelinq.features.auth.domain;

public interface AuthMailSender {
    void sendMagicLink(String email, String verifyUrl);
}

