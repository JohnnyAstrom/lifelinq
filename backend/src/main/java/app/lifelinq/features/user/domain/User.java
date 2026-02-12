package app.lifelinq.features.user.domain;

import java.util.UUID;

public final class User {
    private final UUID id;

    public User(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
