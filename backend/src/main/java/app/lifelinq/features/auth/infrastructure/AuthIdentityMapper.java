package app.lifelinq.features.auth.infrastructure;

import app.lifelinq.features.auth.domain.AuthIdentity;

final class AuthIdentityMapper {
    AuthIdentityEntity toEntity(AuthIdentity identity) {
        return new AuthIdentityEntity(
                identity.getId(),
                identity.getProvider(),
                identity.getSubject(),
                identity.getUserId()
        );
    }

    AuthIdentity toDomain(AuthIdentityEntity entity) {
        return new AuthIdentity(
                entity.getId(),
                entity.getProvider(),
                entity.getSubject(),
                entity.getUserId()
        );
    }
}

