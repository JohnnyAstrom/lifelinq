package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.MagicLinkChallenge;

final class MagicLinkChallengeMapper {
    MagicLinkChallengeEntity toEntity(MagicLinkChallenge challenge) {
        return new MagicLinkChallengeEntity(
                challenge.getId(),
                challenge.getToken(),
                challenge.getEmail(),
                challenge.getExpiresAt(),
                challenge.getConsumedAt(),
                challenge.getVersion()
        );
    }

    MagicLinkChallenge toDomain(MagicLinkChallengeEntity entity) {
        return new MagicLinkChallenge(
                entity.getId(),
                entity.getToken(),
                entity.getEmail(),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getVersion()
        );
    }
}
