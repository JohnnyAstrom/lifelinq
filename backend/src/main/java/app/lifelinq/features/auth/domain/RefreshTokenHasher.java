package app.lifelinq.features.auth.domain;

public interface RefreshTokenHasher {
    String hash(String plaintextToken);

    boolean matches(String plaintextToken, String hashedToken);
}

