package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;
import app.lifelinq.features.auth.domain.MagicLinkChallengeRepository;
import java.util.Optional;

public final class JpaMagicLinkChallengeRepositoryAdapter implements MagicLinkChallengeRepository {
    private final MagicLinkChallengeJpaRepository repository;
    private final MagicLinkChallengeMapper mapper;

    public JpaMagicLinkChallengeRepositoryAdapter(
            MagicLinkChallengeJpaRepository repository,
            MagicLinkChallengeMapper mapper
    ) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<MagicLinkChallenge> findByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return repository.existsByToken(token);
    }

    @Override
    public void save(MagicLinkChallenge challenge) {
        if (challenge == null) {
            throw new IllegalArgumentException("challenge must not be null");
        }
        repository.save(mapper.toEntity(challenge));
    }
}

