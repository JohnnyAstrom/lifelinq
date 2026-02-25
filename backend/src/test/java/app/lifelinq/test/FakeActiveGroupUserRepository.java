package app.lifelinq.test;

import app.lifelinq.features.user.domain.User;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class FakeActiveGroupUserRepository implements UserRepository {
    private final Map<UUID, User> users = new HashMap<>();

    public FakeActiveGroupUserRepository withUser(UUID userId) {
        users.put(userId, new User(userId));
        return this;
    }

    public FakeActiveGroupUserRepository withUser(UUID userId, UUID activeGroupId) {
        users.put(userId, new User(userId, activeGroupId));
        return this;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void deleteById(UUID id) {
        users.remove(id);
    }
}
