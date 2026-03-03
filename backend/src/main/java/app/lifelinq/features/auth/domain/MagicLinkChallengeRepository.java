package app.lifelinq.features.auth.domain;

import java.util.Optional;

public interface MagicLinkChallengeRepository {
    Optional<MagicLinkChallenge> findByToken(String token);

    boolean existsByToken(String token);

    void save(MagicLinkChallenge challenge);
}

