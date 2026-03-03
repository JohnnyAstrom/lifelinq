package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryMagicLinkChallengeRepository implements MagicLinkChallengeRepository {
    private final Map<String, MagicLinkChallenge> byToken = new HashMap<>();

    @Override
    public Optional<MagicLinkChallenge> findByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return Optional.ofNullable(byToken.get(token));
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return byToken.containsKey(token);
    }

    @Override
    public void save(MagicLinkChallenge challenge) {
        if (challenge == null) {
            throw new IllegalArgumentException("challenge must not be null");
        }
        byToken.put(challenge.getToken(), challenge);
    }
}

