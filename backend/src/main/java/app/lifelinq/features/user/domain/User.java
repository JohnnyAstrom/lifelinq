package app.lifelinq.features.user.domain;

import java.util.UUID;

public final class User {
    private final UUID id;
    private final UUID activeGroupId;

    public User(UUID id) {
        this(id, null);
    }

    public User(UUID id, UUID activeGroupId) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.activeGroupId = activeGroupId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActiveGroupId() {
        return activeGroupId;
    }

    public User withActiveGroupId(UUID activeGroupId) {
        return new User(id, activeGroupId);
    }
}
