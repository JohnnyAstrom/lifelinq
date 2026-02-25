package app.lifelinq.features.group.domain;

import java.util.UUID;

public final class Group {
    private final UUID id;
    private final String name;

    public Group(UUID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
