package app.lifelinq.features.household.domain;

import java.util.UUID;

public final class Household {
    private final UUID id;
    private final String name;

    public Household(UUID id, String name) {
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
