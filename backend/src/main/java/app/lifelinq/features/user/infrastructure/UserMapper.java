package app.lifelinq.features.user.infrastructure;

import app.lifelinq.features.user.domain.User;

final class UserMapper {
    UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getActiveGroupId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getActiveGroupId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName()
        );
    }
}
